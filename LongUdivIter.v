// Generator : SpinalHDL v1.9.2    git head : 457a28dd4b2ae1f3a1f3ef4268c3a7f613ec81ed
// Component : LongUdivIter
// Git hash  : 12c86b140cda36662c3e8d553e58dfcc2d019a36

`timescale 1ns/1ps

module LongUdivIter (
  input               io_ofwdMvp,
  input      [3:0]    io_itdIn_tempNumer,
  input      [3:0]    io_itdIn_tempQuot,
  input      [3:0]    io_itdIn_tempRema,
  input      [5:0]    io_itdIn_denomMultLut_0,
  input      [5:0]    io_itdIn_denomMultLut_1,
  input      [5:0]    io_itdIn_denomMultLut_2,
  input      [5:0]    io_itdIn_denomMultLut_3,
  input      [7:0]    io_itdIn_tag,
  input      [3:0]    io_itdIn_formal_formalNumer,
  input      [3:0]    io_itdIn_formal_formalDenom,
  input      [3:0]    io_itdIn_formal_oracleQuot,
  input      [3:0]    io_itdIn_formal_oracleRema,
  input      [5:0]    io_itdIn_formal_formalDenomMultLut_0,
  input      [5:0]    io_itdIn_formal_formalDenomMultLut_1,
  input      [5:0]    io_itdIn_formal_formalDenomMultLut_2,
  input      [5:0]    io_itdIn_formal_formalDenomMultLut_3,
  input      [2:0]    io_chunkStart,
  output     [3:0]    io_itdOut_tempNumer,
  output     [3:0]    io_itdOut_tempQuot,
  output     [3:0]    io_itdOut_tempRema,
  output     [5:0]    io_itdOut_denomMultLut_0,
  output     [5:0]    io_itdOut_denomMultLut_1,
  output     [5:0]    io_itdOut_denomMultLut_2,
  output     [5:0]    io_itdOut_denomMultLut_3,
  output     [7:0]    io_itdOut_tag,
  output     [3:0]    io_itdOut_formal_formalNumer,
  output     [3:0]    io_itdOut_formal_formalDenom,
  output     [3:0]    io_itdOut_formal_oracleQuot,
  output     [3:0]    io_itdOut_formal_oracleRema,
  output     [5:0]    io_itdOut_formal_formalDenomMultLut_0,
  output     [5:0]    io_itdOut_formal_formalDenomMultLut_1,
  output     [5:0]    io_itdOut_formal_formalDenomMultLut_2,
  output     [5:0]    io_itdOut_formal_formalDenomMultLut_3,
  output reg [1:0]    io_quotDigit,
  output     [3:0]    io_shiftInRema,
  output reg [3:0]    io_gtVec
);

  wire       [4:0]    _zz_subdivTempNumer;
  wire       [5:0]    _zz_io_gtVec;
  wire       [5:0]    _zz_io_gtVec_1;
  wire       [5:0]    _zz_io_gtVec_2;
  wire       [5:0]    _zz_io_gtVec_3;
  wire       [5:0]    _zz_io_itdOut_tempRema;
  wire       [5:0]    _zz_io_itdOut_tempRema_1;
  reg        [5:0]    _zz_io_itdOut_tempRema_2;
  wire       [1:0]    subdivTempNumer;

  assign _zz_subdivTempNumer = (io_chunkStart * 2'b10);
  assign _zz_io_gtVec = {2'd0, io_shiftInRema};
  assign _zz_io_gtVec_1 = {2'd0, io_shiftInRema};
  assign _zz_io_gtVec_2 = {2'd0, io_shiftInRema};
  assign _zz_io_gtVec_3 = {2'd0, io_shiftInRema};
  assign _zz_io_itdOut_tempRema = (_zz_io_itdOut_tempRema_1 - _zz_io_itdOut_tempRema_2);
  assign _zz_io_itdOut_tempRema_1 = {2'd0, io_shiftInRema};
  always @(*) begin
    case(io_quotDigit)
      2'b00 : _zz_io_itdOut_tempRema_2 = io_itdIn_denomMultLut_0;
      2'b01 : _zz_io_itdOut_tempRema_2 = io_itdIn_denomMultLut_1;
      2'b10 : _zz_io_itdOut_tempRema_2 = io_itdIn_denomMultLut_2;
      default : _zz_io_itdOut_tempRema_2 = io_itdIn_denomMultLut_3;
    endcase
  end

  assign subdivTempNumer = io_itdIn_tempNumer[_zz_subdivTempNumer +: 2];
  assign io_shiftInRema = {subdivTempNumer,io_itdIn_tempRema[1 : 0]};
  always @(*) begin
    io_gtVec[0] = (_zz_io_gtVec < io_itdIn_denomMultLut_0);
    io_gtVec[1] = (_zz_io_gtVec_1 < io_itdIn_denomMultLut_1);
    io_gtVec[2] = (_zz_io_gtVec_2 < io_itdIn_denomMultLut_2);
    io_gtVec[3] = (_zz_io_gtVec_3 < io_itdIn_denomMultLut_3);
  end

  always @(*) begin
    case(io_gtVec)
      4'b1110 : begin
        io_quotDigit = 2'b00;
      end
      4'b1100 : begin
        io_quotDigit = 2'b01;
      end
      4'b1000 : begin
        io_quotDigit = 2'b10;
      end
      4'b0000 : begin
        io_quotDigit = 2'b11;
      end
      default : begin
        io_quotDigit = 2'b00;
      end
    endcase
  end

  assign io_itdOut_tempQuot = io_itdIn_tempQuot;
  assign io_itdOut_tempNumer = io_itdIn_tempNumer;
  assign io_itdOut_tempRema = _zz_io_itdOut_tempRema[3 : 0];
  assign io_itdOut_denomMultLut_0 = io_itdIn_denomMultLut_0;
  assign io_itdOut_denomMultLut_1 = io_itdIn_denomMultLut_1;
  assign io_itdOut_denomMultLut_2 = io_itdIn_denomMultLut_2;
  assign io_itdOut_denomMultLut_3 = io_itdIn_denomMultLut_3;
  assign io_itdOut_tag = io_itdIn_tag;
  assign io_itdOut_formal_formalNumer = io_itdIn_formal_formalNumer;
  assign io_itdOut_formal_formalDenom = io_itdIn_formal_formalDenom;
  assign io_itdOut_formal_oracleQuot = io_itdIn_formal_oracleQuot;
  assign io_itdOut_formal_oracleRema = io_itdIn_formal_oracleRema;
  assign io_itdOut_formal_formalDenomMultLut_0 = io_itdIn_formal_formalDenomMultLut_0;
  assign io_itdOut_formal_formalDenomMultLut_1 = io_itdIn_formal_formalDenomMultLut_1;
  assign io_itdOut_formal_formalDenomMultLut_2 = io_itdIn_formal_formalDenomMultLut_2;
  assign io_itdOut_formal_formalDenomMultLut_3 = io_itdIn_formal_formalDenomMultLut_3;

endmodule
