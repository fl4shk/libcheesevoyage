#!/usr/bin/env python3

from enum import Enum, auto, unique
import math

from misc_util import *
from nmigen import *
from nmigen.hdl.rec import *

from misc_util import *
from general.fifo_mods import *
#--------
class SdramParams:
	def __init__(self, ROW_WIDTH, COL_WIDTH, BANK_WIDTH, DQM_WIDTH,
		WORD_WIDTH, T_RCD, T_RP):
		self.__ROW_WIDTH, self.__COL_WIDTH, self.__BANK_WIDTH, \
			self.__DQM_WIDTH, self.__WORD_WIDTH, self.__T_RCD, \
			self.__T_RP \
			= ROW_WIDTH, COL_WIDTH, BANK_WIDTH, DQM_WIDTH, WORD_WIDTH, \
			T_RCD, T_RP

	def ROW_WIDTH(self):
		return self.__ROW_WIDTH
	def COL_WIDTH(self):
		return self.__COL_WIDTH
	def BANK_WIDTH(self):
		return self.__BANK_WIDTH
	def DQM_WIDTH(self):
		return self.__DQM_WIDTH
	def WORD_WIDTH(self):
		return self.__WORD_WIDTH

	# Active to read/write
	def T_RCD(self):
		return self.__T_RCD

	# Precharge to active
	def T_RP(self):
		return self.__T_RP
#--------
class SdramBus:
	def __init__(self, PARAMS):
		self.__PARAMS = PARAMS

		self.a = Signal(max(self.PARAMS().ROW_WIDTH(),
			self.PARAMS().COL_WIDTH()))
		self.bank = Signal(self.PARAMS().BANK_WIDTH())

		self.cas_n = Signal()
		self.cke = Signal()
		self.clk = Signal()

		self.cs_n = Signal()

		self.dqm = Signal(self.PARAMS().ROW_WIDTH())

		self.dq = Signal(self.PARAMS().WORD_WIDTH())

		self.ras_n = Signal()
		self.we_n = Signal()

	def PARAMS(self):
		return self.__PARAMS
#--------
class SdramCtrlPubBus:
	def __init__(self, PARAMS):
		self.__PARAMS = PARAMS

		self.wr_addr = Signal(self.ADDR_WIDTH())
		self.wr_data = Signal(self.WORD_WIDTH())
		self.wr_req = Signal()
		self.wr_gnt = Signal()

		self.rd_addr = Signal(self.ADDR_WIDTH())
		self.rd_data = Signal(self.WORD_WIDTH())
		self.rd_req = Signal()
		self.rd_gnt = Signal()
		self.rd_valid = Signal()

	def PARAMS(self):
		return self.__PARAMS

	def ADDR_WIDTH(self):
		return self.PARAMS().ROW_WIDTH() + self.PARAMS().COL_WIDTH() \
			+ self.PARAMS().BANK_WIDTH()
	def WORD_WIDTH(self):
		return self.PARAMS().WORD_WIDTH()
#--------
class SdramCtrlBus:
	def __init__(self, PARAMS):
		self.sdram = SdramBus(PARAMS)
		self.pub = SdramCtrlPubBus(PARAMS)

	def PARAMS(self):
		return self.pub.PARAMS()
	def ROW_WIDTH(self):
		return self.PARAMS().ROW_WIDTH()
	def COL_WIDTH(self):
		return self.PARAMS().COL_WIDTH()
	def BANK_WIDTH(self):
		return self.PARAMS().BANK_WIDTH()

	def COL_START(self):
		return 0
	def COL_END_P_1(self):
		return self.COL_WIDTH()
	def ROW_START(self):
		return self.COL_END_P_1()
	def ROW_END_P_1(self):
		return (self.ROW_START() + self.ROW_WIDTH())
	def BANK_START(self):
		return self.ROW_END_P_1()
	def BANK_END_P_1(self):
		return (self.BANK_START() + self.BANK_WIDTH())

	def wr_addr_col(self):
		return self.__col(self.pub.wr_addr)
	def wr_addr_row(self):
		return self.__row(self.pub.wr_addr)
	def wr_addr_bank(self):
		return self.__bank(self.pub.wr_addr)

	def rd_addr_col(self):
		return self.__col(self.pub.rd_addr)
	def rd_addr_row(self):
		return self.__row(self.pub.rd_addr)
	def rd_addr_bank(self):
		return self.__bank(self.pub.rd_addr)

	def __col(self, addr):
		return addr[self.COL_START():self.COL_END_P_1()]
	def __row(self, addr):
		return addr[self.ROW_START():self.ROW_END_P_1()]
	def __bank(self, addr):
		return addr[self.BANK_START():self.BANK_END_P_1()]
#--------
class SdramCtrl(Elaboratable):
	def __init__(self, CLK_RATE, PARAMS):
		self.__bus = SdramCtrlBus(PARAMS=PARAMS)
		self.__CLK_RATE = CLK_RATE

		self.__CLK_PERIOD = 1 / self.CLK_RATE()

	def bus(self):
		return self.__bus
	def PARAMS(self):
		return self.bus().PARAMS()
	def CLK_RATE(self):
		return self.__CLK_RATE
	def CLK_PERIOD(self):
		return self.__CLK_PERIOD
#--------
