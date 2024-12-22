// Generator : SpinalHDL dev    git head : 40fffe001aeaad57333b030692d82645934a32e1
// Component : TopLevel
// Git hash  : 8150cbc31bcd0385a7234f2b4c7189b29982a3b5

`timescale 1ns/1ps

typedef struct {

  logic  [7:0]    myData ;

} TestStreamPayloadInner;

typedef struct {

  TestStreamPayloadInner inpData_0[3];
  TestStreamPayloadInner inpData_1[3];
  TestStreamPayloadInner outpData_0[3];
  TestStreamPayloadInner outpData_1[3];

} TestStreamPayloadInner_1;

interface TopLevelIo_8_3 () ;

  TestStreamPayloadInner_1 tempStruct;
  Stream_3        stm0();
  Stream_2        stm1();
  Stream_1        push();
  Stream_1        pop();

endinterface

interface Stream () ;

  logic           valid ;
  logic           ready ;
  logic  [7:0]    payload ;

endinterface

interface Stream_1 () ;

  logic           valid ;
  logic           ready ;
  TestStreamPayloadInner_1 payload[3];

endinterface

interface Stream_2 () ;

  Stream_4        stmData();

endinterface

interface Stream_3 () ;

  Stream          stmData();

endinterface

interface Stream_4 () ;

  logic           valid ;
  logic           ready ;
  logic  [8:0]    payload ;

endinterface


module TopLevel (
  input  wire [7:0]    io_tempStruct_inpData_0_0_myData,
  input  wire [7:0]    io_tempStruct_inpData_0_1_myData,
  input  wire [7:0]    io_tempStruct_inpData_0_2_myData,
  input  wire [7:0]    io_tempStruct_inpData_1_0_myData,
  input  wire [7:0]    io_tempStruct_inpData_1_1_myData,
  input  wire [7:0]    io_tempStruct_inpData_1_2_myData,
  input  wire [7:0]    io_tempStruct_outpData_0_0_myData,
  input  wire [7:0]    io_tempStruct_outpData_0_1_myData,
  input  wire [7:0]    io_tempStruct_outpData_0_2_myData,
  input  wire [7:0]    io_tempStruct_outpData_1_0_myData,
  input  wire [7:0]    io_tempStruct_outpData_1_1_myData,
  input  wire [7:0]    io_tempStruct_outpData_1_2_myData,
  input  wire          io_stm0_stmData_valid,
  input  wire          io_stm0_stmData_ready,
  input  wire [7:0]    io_stm0_stmData_payload,
  input  wire          io_stm1_stmData_valid,
  input  wire          io_stm1_stmData_ready,
  input  wire [8:0]    io_stm1_stmData_payload,
  input  wire          io_push_valid,
  output wire          io_push_ready,
  input  wire [7:0]    io_push_payload_0_inpData_0_0_myData,
  input  wire [7:0]    io_push_payload_0_inpData_0_1_myData,
  input  wire [7:0]    io_push_payload_0_inpData_0_2_myData,
  input  wire [7:0]    io_push_payload_0_inpData_1_0_myData,
  input  wire [7:0]    io_push_payload_0_inpData_1_1_myData,
  input  wire [7:0]    io_push_payload_0_inpData_1_2_myData,
  input  wire [7:0]    io_push_payload_0_outpData_0_0_myData,
  input  wire [7:0]    io_push_payload_0_outpData_0_1_myData,
  input  wire [7:0]    io_push_payload_0_outpData_0_2_myData,
  input  wire [7:0]    io_push_payload_0_outpData_1_0_myData,
  input  wire [7:0]    io_push_payload_0_outpData_1_1_myData,
  input  wire [7:0]    io_push_payload_0_outpData_1_2_myData,
  input  wire [7:0]    io_push_payload_1_inpData_0_0_myData,
  input  wire [7:0]    io_push_payload_1_inpData_0_1_myData,
  input  wire [7:0]    io_push_payload_1_inpData_0_2_myData,
  input  wire [7:0]    io_push_payload_1_inpData_1_0_myData,
  input  wire [7:0]    io_push_payload_1_inpData_1_1_myData,
  input  wire [7:0]    io_push_payload_1_inpData_1_2_myData,
  input  wire [7:0]    io_push_payload_1_outpData_0_0_myData,
  input  wire [7:0]    io_push_payload_1_outpData_0_1_myData,
  input  wire [7:0]    io_push_payload_1_outpData_0_2_myData,
  input  wire [7:0]    io_push_payload_1_outpData_1_0_myData,
  input  wire [7:0]    io_push_payload_1_outpData_1_1_myData,
  input  wire [7:0]    io_push_payload_1_outpData_1_2_myData,
  input  wire [7:0]    io_push_payload_2_inpData_0_0_myData,
  input  wire [7:0]    io_push_payload_2_inpData_0_1_myData,
  input  wire [7:0]    io_push_payload_2_inpData_0_2_myData,
  input  wire [7:0]    io_push_payload_2_inpData_1_0_myData,
  input  wire [7:0]    io_push_payload_2_inpData_1_1_myData,
  input  wire [7:0]    io_push_payload_2_inpData_1_2_myData,
  input  wire [7:0]    io_push_payload_2_outpData_0_0_myData,
  input  wire [7:0]    io_push_payload_2_outpData_0_1_myData,
  input  wire [7:0]    io_push_payload_2_outpData_0_2_myData,
  input  wire [7:0]    io_push_payload_2_outpData_1_0_myData,
  input  wire [7:0]    io_push_payload_2_outpData_1_1_myData,
  input  wire [7:0]    io_push_payload_2_outpData_1_2_myData,
  output wire          io_pop_valid,
  input  wire          io_pop_ready,
  output wire [7:0]    io_pop_payload_0_inpData_0_0_myData,
  output wire [7:0]    io_pop_payload_0_inpData_0_1_myData,
  output wire [7:0]    io_pop_payload_0_inpData_0_2_myData,
  output wire [7:0]    io_pop_payload_0_inpData_1_0_myData,
  output wire [7:0]    io_pop_payload_0_inpData_1_1_myData,
  output wire [7:0]    io_pop_payload_0_inpData_1_2_myData,
  output wire [7:0]    io_pop_payload_0_outpData_0_0_myData,
  output wire [7:0]    io_pop_payload_0_outpData_0_1_myData,
  output wire [7:0]    io_pop_payload_0_outpData_0_2_myData,
  output wire [7:0]    io_pop_payload_0_outpData_1_0_myData,
  output wire [7:0]    io_pop_payload_0_outpData_1_1_myData,
  output wire [7:0]    io_pop_payload_0_outpData_1_2_myData,
  output wire [7:0]    io_pop_payload_1_inpData_0_0_myData,
  output wire [7:0]    io_pop_payload_1_inpData_0_1_myData,
  output wire [7:0]    io_pop_payload_1_inpData_0_2_myData,
  output wire [7:0]    io_pop_payload_1_inpData_1_0_myData,
  output wire [7:0]    io_pop_payload_1_inpData_1_1_myData,
  output wire [7:0]    io_pop_payload_1_inpData_1_2_myData,
  output wire [7:0]    io_pop_payload_1_outpData_0_0_myData,
  output wire [7:0]    io_pop_payload_1_outpData_0_1_myData,
  output wire [7:0]    io_pop_payload_1_outpData_0_2_myData,
  output wire [7:0]    io_pop_payload_1_outpData_1_0_myData,
  output wire [7:0]    io_pop_payload_1_outpData_1_1_myData,
  output wire [7:0]    io_pop_payload_1_outpData_1_2_myData,
  output wire [7:0]    io_pop_payload_2_inpData_0_0_myData,
  output wire [7:0]    io_pop_payload_2_inpData_0_1_myData,
  output wire [7:0]    io_pop_payload_2_inpData_0_2_myData,
  output wire [7:0]    io_pop_payload_2_inpData_1_0_myData,
  output wire [7:0]    io_pop_payload_2_inpData_1_1_myData,
  output wire [7:0]    io_pop_payload_2_inpData_1_2_myData,
  output wire [7:0]    io_pop_payload_2_outpData_0_0_myData,
  output wire [7:0]    io_pop_payload_2_outpData_0_1_myData,
  output wire [7:0]    io_pop_payload_2_outpData_0_2_myData,
  output wire [7:0]    io_pop_payload_2_outpData_1_0_myData,
  output wire [7:0]    io_pop_payload_2_outpData_1_1_myData,
  output wire [7:0]    io_pop_payload_2_outpData_1_2_myData,
  input  wire          clk,
  input  wire          reset
);

  TopLevelIo_8_3      dut_io();

  TopLevelInnards dut (
    .io                                      (dut_io), //TopLevelIo_8_3.mst
    .clk                                     (clk   ), //i
    .reset                                   (reset )  //i
  );
  assign dut_io.tempStruct.inpData_0[0].myData = io_tempStruct_inpData_0_0_myData;
  assign dut_io.tempStruct.inpData_0[1].myData = io_tempStruct_inpData_0_1_myData;
  assign dut_io.tempStruct.inpData_0[2].myData = io_tempStruct_inpData_0_2_myData;
  assign dut_io.tempStruct.inpData_1[0].myData = io_tempStruct_inpData_1_0_myData;
  assign dut_io.tempStruct.inpData_1[1].myData = io_tempStruct_inpData_1_1_myData;
  assign dut_io.tempStruct.inpData_1[2].myData = io_tempStruct_inpData_1_2_myData;
  assign dut_io.tempStruct.outpData_0[0].myData = io_tempStruct_outpData_0_0_myData;
  assign dut_io.tempStruct.outpData_0[1].myData = io_tempStruct_outpData_0_1_myData;
  assign dut_io.tempStruct.outpData_0[2].myData = io_tempStruct_outpData_0_2_myData;
  assign dut_io.tempStruct.outpData_1[0].myData = io_tempStruct_outpData_1_0_myData;
  assign dut_io.tempStruct.outpData_1[1].myData = io_tempStruct_outpData_1_1_myData;
  assign dut_io.tempStruct.outpData_1[2].myData = io_tempStruct_outpData_1_2_myData;
  assign dut_io.stm0.stmData.valid = io_stm0_stmData_valid;
  assign dut_io.stm0.stmData.ready = io_stm0_stmData_ready;
  assign dut_io.stm0.stmData.payload = io_stm0_stmData_payload;
  assign dut_io.stm1.stmData.valid = io_stm1_stmData_valid;
  assign dut_io.stm1.stmData.ready = io_stm1_stmData_ready;
  assign dut_io.stm1.stmData.payload = io_stm1_stmData_payload;
  assign dut_io.push.valid = io_push_valid;
  assign io_push_ready = dut_io.push.ready;
  assign dut_io.push.payload[0].inpData_0[0].myData = io_push_payload_0_inpData_0_0_myData;
  assign dut_io.push.payload[0].inpData_0[1].myData = io_push_payload_0_inpData_0_1_myData;
  assign dut_io.push.payload[0].inpData_0[2].myData = io_push_payload_0_inpData_0_2_myData;
  assign dut_io.push.payload[0].inpData_1[0].myData = io_push_payload_0_inpData_1_0_myData;
  assign dut_io.push.payload[0].inpData_1[1].myData = io_push_payload_0_inpData_1_1_myData;
  assign dut_io.push.payload[0].inpData_1[2].myData = io_push_payload_0_inpData_1_2_myData;
  assign dut_io.push.payload[0].outpData_0[0].myData = io_push_payload_0_outpData_0_0_myData;
  assign dut_io.push.payload[0].outpData_0[1].myData = io_push_payload_0_outpData_0_1_myData;
  assign dut_io.push.payload[0].outpData_0[2].myData = io_push_payload_0_outpData_0_2_myData;
  assign dut_io.push.payload[0].outpData_1[0].myData = io_push_payload_0_outpData_1_0_myData;
  assign dut_io.push.payload[0].outpData_1[1].myData = io_push_payload_0_outpData_1_1_myData;
  assign dut_io.push.payload[0].outpData_1[2].myData = io_push_payload_0_outpData_1_2_myData;
  assign dut_io.push.payload[1].inpData_0[0].myData = io_push_payload_1_inpData_0_0_myData;
  assign dut_io.push.payload[1].inpData_0[1].myData = io_push_payload_1_inpData_0_1_myData;
  assign dut_io.push.payload[1].inpData_0[2].myData = io_push_payload_1_inpData_0_2_myData;
  assign dut_io.push.payload[1].inpData_1[0].myData = io_push_payload_1_inpData_1_0_myData;
  assign dut_io.push.payload[1].inpData_1[1].myData = io_push_payload_1_inpData_1_1_myData;
  assign dut_io.push.payload[1].inpData_1[2].myData = io_push_payload_1_inpData_1_2_myData;
  assign dut_io.push.payload[1].outpData_0[0].myData = io_push_payload_1_outpData_0_0_myData;
  assign dut_io.push.payload[1].outpData_0[1].myData = io_push_payload_1_outpData_0_1_myData;
  assign dut_io.push.payload[1].outpData_0[2].myData = io_push_payload_1_outpData_0_2_myData;
  assign dut_io.push.payload[1].outpData_1[0].myData = io_push_payload_1_outpData_1_0_myData;
  assign dut_io.push.payload[1].outpData_1[1].myData = io_push_payload_1_outpData_1_1_myData;
  assign dut_io.push.payload[1].outpData_1[2].myData = io_push_payload_1_outpData_1_2_myData;
  assign dut_io.push.payload[2].inpData_0[0].myData = io_push_payload_2_inpData_0_0_myData;
  assign dut_io.push.payload[2].inpData_0[1].myData = io_push_payload_2_inpData_0_1_myData;
  assign dut_io.push.payload[2].inpData_0[2].myData = io_push_payload_2_inpData_0_2_myData;
  assign dut_io.push.payload[2].inpData_1[0].myData = io_push_payload_2_inpData_1_0_myData;
  assign dut_io.push.payload[2].inpData_1[1].myData = io_push_payload_2_inpData_1_1_myData;
  assign dut_io.push.payload[2].inpData_1[2].myData = io_push_payload_2_inpData_1_2_myData;
  assign dut_io.push.payload[2].outpData_0[0].myData = io_push_payload_2_outpData_0_0_myData;
  assign dut_io.push.payload[2].outpData_0[1].myData = io_push_payload_2_outpData_0_1_myData;
  assign dut_io.push.payload[2].outpData_0[2].myData = io_push_payload_2_outpData_0_2_myData;
  assign dut_io.push.payload[2].outpData_1[0].myData = io_push_payload_2_outpData_1_0_myData;
  assign dut_io.push.payload[2].outpData_1[1].myData = io_push_payload_2_outpData_1_1_myData;
  assign dut_io.push.payload[2].outpData_1[2].myData = io_push_payload_2_outpData_1_2_myData;
  assign io_pop_valid = dut_io.pop.valid;
  assign dut_io.pop.ready = io_pop_ready;
  assign io_pop_payload_0_inpData_0_0_myData = dut_io.pop.payload[0].inpData_0[0].myData;
  assign io_pop_payload_0_inpData_0_1_myData = dut_io.pop.payload[0].inpData_0[1].myData;
  assign io_pop_payload_0_inpData_0_2_myData = dut_io.pop.payload[0].inpData_0[2].myData;
  assign io_pop_payload_0_inpData_1_0_myData = dut_io.pop.payload[0].inpData_1[0].myData;
  assign io_pop_payload_0_inpData_1_1_myData = dut_io.pop.payload[0].inpData_1[1].myData;
  assign io_pop_payload_0_inpData_1_2_myData = dut_io.pop.payload[0].inpData_1[2].myData;
  assign io_pop_payload_0_outpData_0_0_myData = dut_io.pop.payload[0].outpData_0[0].myData;
  assign io_pop_payload_0_outpData_0_1_myData = dut_io.pop.payload[0].outpData_0[1].myData;
  assign io_pop_payload_0_outpData_0_2_myData = dut_io.pop.payload[0].outpData_0[2].myData;
  assign io_pop_payload_0_outpData_1_0_myData = dut_io.pop.payload[0].outpData_1[0].myData;
  assign io_pop_payload_0_outpData_1_1_myData = dut_io.pop.payload[0].outpData_1[1].myData;
  assign io_pop_payload_0_outpData_1_2_myData = dut_io.pop.payload[0].outpData_1[2].myData;
  assign io_pop_payload_1_inpData_0_0_myData = dut_io.pop.payload[1].inpData_0[0].myData;
  assign io_pop_payload_1_inpData_0_1_myData = dut_io.pop.payload[1].inpData_0[1].myData;
  assign io_pop_payload_1_inpData_0_2_myData = dut_io.pop.payload[1].inpData_0[2].myData;
  assign io_pop_payload_1_inpData_1_0_myData = dut_io.pop.payload[1].inpData_1[0].myData;
  assign io_pop_payload_1_inpData_1_1_myData = dut_io.pop.payload[1].inpData_1[1].myData;
  assign io_pop_payload_1_inpData_1_2_myData = dut_io.pop.payload[1].inpData_1[2].myData;
  assign io_pop_payload_1_outpData_0_0_myData = dut_io.pop.payload[1].outpData_0[0].myData;
  assign io_pop_payload_1_outpData_0_1_myData = dut_io.pop.payload[1].outpData_0[1].myData;
  assign io_pop_payload_1_outpData_0_2_myData = dut_io.pop.payload[1].outpData_0[2].myData;
  assign io_pop_payload_1_outpData_1_0_myData = dut_io.pop.payload[1].outpData_1[0].myData;
  assign io_pop_payload_1_outpData_1_1_myData = dut_io.pop.payload[1].outpData_1[1].myData;
  assign io_pop_payload_1_outpData_1_2_myData = dut_io.pop.payload[1].outpData_1[2].myData;
  assign io_pop_payload_2_inpData_0_0_myData = dut_io.pop.payload[2].inpData_0[0].myData;
  assign io_pop_payload_2_inpData_0_1_myData = dut_io.pop.payload[2].inpData_0[1].myData;
  assign io_pop_payload_2_inpData_0_2_myData = dut_io.pop.payload[2].inpData_0[2].myData;
  assign io_pop_payload_2_inpData_1_0_myData = dut_io.pop.payload[2].inpData_1[0].myData;
  assign io_pop_payload_2_inpData_1_1_myData = dut_io.pop.payload[2].inpData_1[1].myData;
  assign io_pop_payload_2_inpData_1_2_myData = dut_io.pop.payload[2].inpData_1[2].myData;
  assign io_pop_payload_2_outpData_0_0_myData = dut_io.pop.payload[2].outpData_0[0].myData;
  assign io_pop_payload_2_outpData_0_1_myData = dut_io.pop.payload[2].outpData_0[1].myData;
  assign io_pop_payload_2_outpData_0_2_myData = dut_io.pop.payload[2].outpData_0[2].myData;
  assign io_pop_payload_2_outpData_1_0_myData = dut_io.pop.payload[2].outpData_1[0].myData;
  assign io_pop_payload_2_outpData_1_1_myData = dut_io.pop.payload[2].outpData_1[1].myData;
  assign io_pop_payload_2_outpData_1_2_myData = dut_io.pop.payload[2].outpData_1[2].myData;

endmodule

module TopLevelInnards (
  TopLevelIo_8_3       io,
  input  wire          clk,
  input  wire          reset
);

  Stream_1            pushMidStm();
  Stream_1            popMidStm();
  Stream_1            io_push_s2mPipe();
  reg                 io_push_rValidN;
  TestStreamPayloadInner_1 io_push_rData_0;
  TestStreamPayloadInner_1 io_push_rData_1;
  TestStreamPayloadInner_1 io_push_rData_2;
  TestStreamPayloadInner_1 zzz_Interface_TestStreamPayloadInner;
  TestStreamPayloadInner_1 zzz_Interface_TestStreamPayloadInner_1;
  TestStreamPayloadInner_1 zzz_Interface_TestStreamPayloadInner_2;
  Stream_1            io_push_s2mPipe_m2sPipe();
  reg                 io_push_s2mPipe_rValid;
  TestStreamPayloadInner_1 io_push_s2mPipe_rData_0;
  TestStreamPayloadInner_1 io_push_s2mPipe_rData_1;
  TestStreamPayloadInner_1 io_push_s2mPipe_rData_2;
  wire                when_Stream_l401;
  Stream_1            popMidStm_s2mPipe();
  reg                 popMidStm_rValidN;
  TestStreamPayloadInner_1 popMidStm_rData_0;
  TestStreamPayloadInner_1 popMidStm_rData_1;
  TestStreamPayloadInner_1 popMidStm_rData_2;
  TestStreamPayloadInner_1 zzz_Interface_TestStreamPayloadInner_3;
  TestStreamPayloadInner_1 zzz_Interface_TestStreamPayloadInner_4;
  TestStreamPayloadInner_1 zzz_Interface_TestStreamPayloadInner_5;
  Stream_1            popMidStm_s2mPipe_m2sPipe();
  reg                 popMidStm_s2mPipe_rValid;
  TestStreamPayloadInner_1 popMidStm_s2mPipe_rData_0;
  TestStreamPayloadInner_1 popMidStm_s2mPipe_rData_1;
  TestStreamPayloadInner_1 popMidStm_s2mPipe_rData_2;
  wire                when_Stream_l401_1;

  assign io.push.ready = io_push_rValidN;
  assign io_push_s2mPipe.valid = (io.push.valid || (! io_push_rValidN));
  assign zzz_Interface_TestStreamPayloadInner.inpData_0[0].myData = (io_push_rValidN ? io.push.payload[0].inpData_0[0].myData : io_push_rData_0.inpData_0[0].myData);
  assign zzz_Interface_TestStreamPayloadInner.inpData_0[1].myData = (io_push_rValidN ? io.push.payload[0].inpData_0[1].myData : io_push_rData_0.inpData_0[1].myData);
  assign zzz_Interface_TestStreamPayloadInner.inpData_0[2].myData = (io_push_rValidN ? io.push.payload[0].inpData_0[2].myData : io_push_rData_0.inpData_0[2].myData);
  assign zzz_Interface_TestStreamPayloadInner.inpData_1[0].myData = (io_push_rValidN ? io.push.payload[0].inpData_1[0].myData : io_push_rData_0.inpData_1[0].myData);
  assign zzz_Interface_TestStreamPayloadInner.inpData_1[1].myData = (io_push_rValidN ? io.push.payload[0].inpData_1[1].myData : io_push_rData_0.inpData_1[1].myData);
  assign zzz_Interface_TestStreamPayloadInner.inpData_1[2].myData = (io_push_rValidN ? io.push.payload[0].inpData_1[2].myData : io_push_rData_0.inpData_1[2].myData);
  assign zzz_Interface_TestStreamPayloadInner.outpData_0[0].myData = (io_push_rValidN ? io.push.payload[0].outpData_0[0].myData : io_push_rData_0.outpData_0[0].myData);
  assign zzz_Interface_TestStreamPayloadInner.outpData_0[1].myData = (io_push_rValidN ? io.push.payload[0].outpData_0[1].myData : io_push_rData_0.outpData_0[1].myData);
  assign zzz_Interface_TestStreamPayloadInner.outpData_0[2].myData = (io_push_rValidN ? io.push.payload[0].outpData_0[2].myData : io_push_rData_0.outpData_0[2].myData);
  assign zzz_Interface_TestStreamPayloadInner.outpData_1[0].myData = (io_push_rValidN ? io.push.payload[0].outpData_1[0].myData : io_push_rData_0.outpData_1[0].myData);
  assign zzz_Interface_TestStreamPayloadInner.outpData_1[1].myData = (io_push_rValidN ? io.push.payload[0].outpData_1[1].myData : io_push_rData_0.outpData_1[1].myData);
  assign zzz_Interface_TestStreamPayloadInner.outpData_1[2].myData = (io_push_rValidN ? io.push.payload[0].outpData_1[2].myData : io_push_rData_0.outpData_1[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_1.inpData_0[0].myData = (io_push_rValidN ? io.push.payload[1].inpData_0[0].myData : io_push_rData_1.inpData_0[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_1.inpData_0[1].myData = (io_push_rValidN ? io.push.payload[1].inpData_0[1].myData : io_push_rData_1.inpData_0[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_1.inpData_0[2].myData = (io_push_rValidN ? io.push.payload[1].inpData_0[2].myData : io_push_rData_1.inpData_0[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_1.inpData_1[0].myData = (io_push_rValidN ? io.push.payload[1].inpData_1[0].myData : io_push_rData_1.inpData_1[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_1.inpData_1[1].myData = (io_push_rValidN ? io.push.payload[1].inpData_1[1].myData : io_push_rData_1.inpData_1[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_1.inpData_1[2].myData = (io_push_rValidN ? io.push.payload[1].inpData_1[2].myData : io_push_rData_1.inpData_1[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_1.outpData_0[0].myData = (io_push_rValidN ? io.push.payload[1].outpData_0[0].myData : io_push_rData_1.outpData_0[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_1.outpData_0[1].myData = (io_push_rValidN ? io.push.payload[1].outpData_0[1].myData : io_push_rData_1.outpData_0[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_1.outpData_0[2].myData = (io_push_rValidN ? io.push.payload[1].outpData_0[2].myData : io_push_rData_1.outpData_0[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_1.outpData_1[0].myData = (io_push_rValidN ? io.push.payload[1].outpData_1[0].myData : io_push_rData_1.outpData_1[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_1.outpData_1[1].myData = (io_push_rValidN ? io.push.payload[1].outpData_1[1].myData : io_push_rData_1.outpData_1[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_1.outpData_1[2].myData = (io_push_rValidN ? io.push.payload[1].outpData_1[2].myData : io_push_rData_1.outpData_1[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_2.inpData_0[0].myData = (io_push_rValidN ? io.push.payload[2].inpData_0[0].myData : io_push_rData_2.inpData_0[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_2.inpData_0[1].myData = (io_push_rValidN ? io.push.payload[2].inpData_0[1].myData : io_push_rData_2.inpData_0[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_2.inpData_0[2].myData = (io_push_rValidN ? io.push.payload[2].inpData_0[2].myData : io_push_rData_2.inpData_0[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_2.inpData_1[0].myData = (io_push_rValidN ? io.push.payload[2].inpData_1[0].myData : io_push_rData_2.inpData_1[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_2.inpData_1[1].myData = (io_push_rValidN ? io.push.payload[2].inpData_1[1].myData : io_push_rData_2.inpData_1[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_2.inpData_1[2].myData = (io_push_rValidN ? io.push.payload[2].inpData_1[2].myData : io_push_rData_2.inpData_1[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_2.outpData_0[0].myData = (io_push_rValidN ? io.push.payload[2].outpData_0[0].myData : io_push_rData_2.outpData_0[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_2.outpData_0[1].myData = (io_push_rValidN ? io.push.payload[2].outpData_0[1].myData : io_push_rData_2.outpData_0[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_2.outpData_0[2].myData = (io_push_rValidN ? io.push.payload[2].outpData_0[2].myData : io_push_rData_2.outpData_0[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_2.outpData_1[0].myData = (io_push_rValidN ? io.push.payload[2].outpData_1[0].myData : io_push_rData_2.outpData_1[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_2.outpData_1[1].myData = (io_push_rValidN ? io.push.payload[2].outpData_1[1].myData : io_push_rData_2.outpData_1[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_2.outpData_1[2].myData = (io_push_rValidN ? io.push.payload[2].outpData_1[2].myData : io_push_rData_2.outpData_1[2].myData);
  assign io_push_s2mPipe.payload[0].inpData_0[0].myData = zzz_Interface_TestStreamPayloadInner.inpData_0[0].myData;
  assign io_push_s2mPipe.payload[0].inpData_0[1].myData = zzz_Interface_TestStreamPayloadInner.inpData_0[1].myData;
  assign io_push_s2mPipe.payload[0].inpData_0[2].myData = zzz_Interface_TestStreamPayloadInner.inpData_0[2].myData;
  assign io_push_s2mPipe.payload[0].inpData_1[0].myData = zzz_Interface_TestStreamPayloadInner.inpData_1[0].myData;
  assign io_push_s2mPipe.payload[0].inpData_1[1].myData = zzz_Interface_TestStreamPayloadInner.inpData_1[1].myData;
  assign io_push_s2mPipe.payload[0].inpData_1[2].myData = zzz_Interface_TestStreamPayloadInner.inpData_1[2].myData;
  assign io_push_s2mPipe.payload[0].outpData_0[0].myData = zzz_Interface_TestStreamPayloadInner.outpData_0[0].myData;
  assign io_push_s2mPipe.payload[0].outpData_0[1].myData = zzz_Interface_TestStreamPayloadInner.outpData_0[1].myData;
  assign io_push_s2mPipe.payload[0].outpData_0[2].myData = zzz_Interface_TestStreamPayloadInner.outpData_0[2].myData;
  assign io_push_s2mPipe.payload[0].outpData_1[0].myData = zzz_Interface_TestStreamPayloadInner.outpData_1[0].myData;
  assign io_push_s2mPipe.payload[0].outpData_1[1].myData = zzz_Interface_TestStreamPayloadInner.outpData_1[1].myData;
  assign io_push_s2mPipe.payload[0].outpData_1[2].myData = zzz_Interface_TestStreamPayloadInner.outpData_1[2].myData;
  assign io_push_s2mPipe.payload[1].inpData_0[0].myData = zzz_Interface_TestStreamPayloadInner_1.inpData_0[0].myData;
  assign io_push_s2mPipe.payload[1].inpData_0[1].myData = zzz_Interface_TestStreamPayloadInner_1.inpData_0[1].myData;
  assign io_push_s2mPipe.payload[1].inpData_0[2].myData = zzz_Interface_TestStreamPayloadInner_1.inpData_0[2].myData;
  assign io_push_s2mPipe.payload[1].inpData_1[0].myData = zzz_Interface_TestStreamPayloadInner_1.inpData_1[0].myData;
  assign io_push_s2mPipe.payload[1].inpData_1[1].myData = zzz_Interface_TestStreamPayloadInner_1.inpData_1[1].myData;
  assign io_push_s2mPipe.payload[1].inpData_1[2].myData = zzz_Interface_TestStreamPayloadInner_1.inpData_1[2].myData;
  assign io_push_s2mPipe.payload[1].outpData_0[0].myData = zzz_Interface_TestStreamPayloadInner_1.outpData_0[0].myData;
  assign io_push_s2mPipe.payload[1].outpData_0[1].myData = zzz_Interface_TestStreamPayloadInner_1.outpData_0[1].myData;
  assign io_push_s2mPipe.payload[1].outpData_0[2].myData = zzz_Interface_TestStreamPayloadInner_1.outpData_0[2].myData;
  assign io_push_s2mPipe.payload[1].outpData_1[0].myData = zzz_Interface_TestStreamPayloadInner_1.outpData_1[0].myData;
  assign io_push_s2mPipe.payload[1].outpData_1[1].myData = zzz_Interface_TestStreamPayloadInner_1.outpData_1[1].myData;
  assign io_push_s2mPipe.payload[1].outpData_1[2].myData = zzz_Interface_TestStreamPayloadInner_1.outpData_1[2].myData;
  assign io_push_s2mPipe.payload[2].inpData_0[0].myData = zzz_Interface_TestStreamPayloadInner_2.inpData_0[0].myData;
  assign io_push_s2mPipe.payload[2].inpData_0[1].myData = zzz_Interface_TestStreamPayloadInner_2.inpData_0[1].myData;
  assign io_push_s2mPipe.payload[2].inpData_0[2].myData = zzz_Interface_TestStreamPayloadInner_2.inpData_0[2].myData;
  assign io_push_s2mPipe.payload[2].inpData_1[0].myData = zzz_Interface_TestStreamPayloadInner_2.inpData_1[0].myData;
  assign io_push_s2mPipe.payload[2].inpData_1[1].myData = zzz_Interface_TestStreamPayloadInner_2.inpData_1[1].myData;
  assign io_push_s2mPipe.payload[2].inpData_1[2].myData = zzz_Interface_TestStreamPayloadInner_2.inpData_1[2].myData;
  assign io_push_s2mPipe.payload[2].outpData_0[0].myData = zzz_Interface_TestStreamPayloadInner_2.outpData_0[0].myData;
  assign io_push_s2mPipe.payload[2].outpData_0[1].myData = zzz_Interface_TestStreamPayloadInner_2.outpData_0[1].myData;
  assign io_push_s2mPipe.payload[2].outpData_0[2].myData = zzz_Interface_TestStreamPayloadInner_2.outpData_0[2].myData;
  assign io_push_s2mPipe.payload[2].outpData_1[0].myData = zzz_Interface_TestStreamPayloadInner_2.outpData_1[0].myData;
  assign io_push_s2mPipe.payload[2].outpData_1[1].myData = zzz_Interface_TestStreamPayloadInner_2.outpData_1[1].myData;
  assign io_push_s2mPipe.payload[2].outpData_1[2].myData = zzz_Interface_TestStreamPayloadInner_2.outpData_1[2].myData;
  always @(*) begin
    io_push_s2mPipe.ready = io_push_s2mPipe_m2sPipe.ready;
    if(when_Stream_l401) begin
      io_push_s2mPipe.ready = 1'b1;
    end
  end

  assign when_Stream_l401 = (! io_push_s2mPipe_m2sPipe.valid);
  assign io_push_s2mPipe_m2sPipe.valid = io_push_s2mPipe_rValid;
  assign io_push_s2mPipe_m2sPipe.payload[0].inpData_0[0].myData = io_push_s2mPipe_rData_0.inpData_0[0].myData;
  assign io_push_s2mPipe_m2sPipe.payload[0].inpData_0[1].myData = io_push_s2mPipe_rData_0.inpData_0[1].myData;
  assign io_push_s2mPipe_m2sPipe.payload[0].inpData_0[2].myData = io_push_s2mPipe_rData_0.inpData_0[2].myData;
  assign io_push_s2mPipe_m2sPipe.payload[0].inpData_1[0].myData = io_push_s2mPipe_rData_0.inpData_1[0].myData;
  assign io_push_s2mPipe_m2sPipe.payload[0].inpData_1[1].myData = io_push_s2mPipe_rData_0.inpData_1[1].myData;
  assign io_push_s2mPipe_m2sPipe.payload[0].inpData_1[2].myData = io_push_s2mPipe_rData_0.inpData_1[2].myData;
  assign io_push_s2mPipe_m2sPipe.payload[0].outpData_0[0].myData = io_push_s2mPipe_rData_0.outpData_0[0].myData;
  assign io_push_s2mPipe_m2sPipe.payload[0].outpData_0[1].myData = io_push_s2mPipe_rData_0.outpData_0[1].myData;
  assign io_push_s2mPipe_m2sPipe.payload[0].outpData_0[2].myData = io_push_s2mPipe_rData_0.outpData_0[2].myData;
  assign io_push_s2mPipe_m2sPipe.payload[0].outpData_1[0].myData = io_push_s2mPipe_rData_0.outpData_1[0].myData;
  assign io_push_s2mPipe_m2sPipe.payload[0].outpData_1[1].myData = io_push_s2mPipe_rData_0.outpData_1[1].myData;
  assign io_push_s2mPipe_m2sPipe.payload[0].outpData_1[2].myData = io_push_s2mPipe_rData_0.outpData_1[2].myData;
  assign io_push_s2mPipe_m2sPipe.payload[1].inpData_0[0].myData = io_push_s2mPipe_rData_1.inpData_0[0].myData;
  assign io_push_s2mPipe_m2sPipe.payload[1].inpData_0[1].myData = io_push_s2mPipe_rData_1.inpData_0[1].myData;
  assign io_push_s2mPipe_m2sPipe.payload[1].inpData_0[2].myData = io_push_s2mPipe_rData_1.inpData_0[2].myData;
  assign io_push_s2mPipe_m2sPipe.payload[1].inpData_1[0].myData = io_push_s2mPipe_rData_1.inpData_1[0].myData;
  assign io_push_s2mPipe_m2sPipe.payload[1].inpData_1[1].myData = io_push_s2mPipe_rData_1.inpData_1[1].myData;
  assign io_push_s2mPipe_m2sPipe.payload[1].inpData_1[2].myData = io_push_s2mPipe_rData_1.inpData_1[2].myData;
  assign io_push_s2mPipe_m2sPipe.payload[1].outpData_0[0].myData = io_push_s2mPipe_rData_1.outpData_0[0].myData;
  assign io_push_s2mPipe_m2sPipe.payload[1].outpData_0[1].myData = io_push_s2mPipe_rData_1.outpData_0[1].myData;
  assign io_push_s2mPipe_m2sPipe.payload[1].outpData_0[2].myData = io_push_s2mPipe_rData_1.outpData_0[2].myData;
  assign io_push_s2mPipe_m2sPipe.payload[1].outpData_1[0].myData = io_push_s2mPipe_rData_1.outpData_1[0].myData;
  assign io_push_s2mPipe_m2sPipe.payload[1].outpData_1[1].myData = io_push_s2mPipe_rData_1.outpData_1[1].myData;
  assign io_push_s2mPipe_m2sPipe.payload[1].outpData_1[2].myData = io_push_s2mPipe_rData_1.outpData_1[2].myData;
  assign io_push_s2mPipe_m2sPipe.payload[2].inpData_0[0].myData = io_push_s2mPipe_rData_2.inpData_0[0].myData;
  assign io_push_s2mPipe_m2sPipe.payload[2].inpData_0[1].myData = io_push_s2mPipe_rData_2.inpData_0[1].myData;
  assign io_push_s2mPipe_m2sPipe.payload[2].inpData_0[2].myData = io_push_s2mPipe_rData_2.inpData_0[2].myData;
  assign io_push_s2mPipe_m2sPipe.payload[2].inpData_1[0].myData = io_push_s2mPipe_rData_2.inpData_1[0].myData;
  assign io_push_s2mPipe_m2sPipe.payload[2].inpData_1[1].myData = io_push_s2mPipe_rData_2.inpData_1[1].myData;
  assign io_push_s2mPipe_m2sPipe.payload[2].inpData_1[2].myData = io_push_s2mPipe_rData_2.inpData_1[2].myData;
  assign io_push_s2mPipe_m2sPipe.payload[2].outpData_0[0].myData = io_push_s2mPipe_rData_2.outpData_0[0].myData;
  assign io_push_s2mPipe_m2sPipe.payload[2].outpData_0[1].myData = io_push_s2mPipe_rData_2.outpData_0[1].myData;
  assign io_push_s2mPipe_m2sPipe.payload[2].outpData_0[2].myData = io_push_s2mPipe_rData_2.outpData_0[2].myData;
  assign io_push_s2mPipe_m2sPipe.payload[2].outpData_1[0].myData = io_push_s2mPipe_rData_2.outpData_1[0].myData;
  assign io_push_s2mPipe_m2sPipe.payload[2].outpData_1[1].myData = io_push_s2mPipe_rData_2.outpData_1[1].myData;
  assign io_push_s2mPipe_m2sPipe.payload[2].outpData_1[2].myData = io_push_s2mPipe_rData_2.outpData_1[2].myData;
  assign pushMidStm.valid = io_push_s2mPipe_m2sPipe.valid;
  assign io_push_s2mPipe_m2sPipe.ready = pushMidStm.ready;
  assign pushMidStm.payload[0].inpData_0[0].myData = io_push_s2mPipe_m2sPipe.payload[0].inpData_0[0].myData;
  assign pushMidStm.payload[0].inpData_0[1].myData = io_push_s2mPipe_m2sPipe.payload[0].inpData_0[1].myData;
  assign pushMidStm.payload[0].inpData_0[2].myData = io_push_s2mPipe_m2sPipe.payload[0].inpData_0[2].myData;
  assign pushMidStm.payload[0].inpData_1[0].myData = io_push_s2mPipe_m2sPipe.payload[0].inpData_1[0].myData;
  assign pushMidStm.payload[0].inpData_1[1].myData = io_push_s2mPipe_m2sPipe.payload[0].inpData_1[1].myData;
  assign pushMidStm.payload[0].inpData_1[2].myData = io_push_s2mPipe_m2sPipe.payload[0].inpData_1[2].myData;
  assign pushMidStm.payload[0].outpData_0[0].myData = io_push_s2mPipe_m2sPipe.payload[0].outpData_0[0].myData;
  assign pushMidStm.payload[0].outpData_0[1].myData = io_push_s2mPipe_m2sPipe.payload[0].outpData_0[1].myData;
  assign pushMidStm.payload[0].outpData_0[2].myData = io_push_s2mPipe_m2sPipe.payload[0].outpData_0[2].myData;
  assign pushMidStm.payload[0].outpData_1[0].myData = io_push_s2mPipe_m2sPipe.payload[0].outpData_1[0].myData;
  assign pushMidStm.payload[0].outpData_1[1].myData = io_push_s2mPipe_m2sPipe.payload[0].outpData_1[1].myData;
  assign pushMidStm.payload[0].outpData_1[2].myData = io_push_s2mPipe_m2sPipe.payload[0].outpData_1[2].myData;
  assign pushMidStm.payload[1].inpData_0[0].myData = io_push_s2mPipe_m2sPipe.payload[1].inpData_0[0].myData;
  assign pushMidStm.payload[1].inpData_0[1].myData = io_push_s2mPipe_m2sPipe.payload[1].inpData_0[1].myData;
  assign pushMidStm.payload[1].inpData_0[2].myData = io_push_s2mPipe_m2sPipe.payload[1].inpData_0[2].myData;
  assign pushMidStm.payload[1].inpData_1[0].myData = io_push_s2mPipe_m2sPipe.payload[1].inpData_1[0].myData;
  assign pushMidStm.payload[1].inpData_1[1].myData = io_push_s2mPipe_m2sPipe.payload[1].inpData_1[1].myData;
  assign pushMidStm.payload[1].inpData_1[2].myData = io_push_s2mPipe_m2sPipe.payload[1].inpData_1[2].myData;
  assign pushMidStm.payload[1].outpData_0[0].myData = io_push_s2mPipe_m2sPipe.payload[1].outpData_0[0].myData;
  assign pushMidStm.payload[1].outpData_0[1].myData = io_push_s2mPipe_m2sPipe.payload[1].outpData_0[1].myData;
  assign pushMidStm.payload[1].outpData_0[2].myData = io_push_s2mPipe_m2sPipe.payload[1].outpData_0[2].myData;
  assign pushMidStm.payload[1].outpData_1[0].myData = io_push_s2mPipe_m2sPipe.payload[1].outpData_1[0].myData;
  assign pushMidStm.payload[1].outpData_1[1].myData = io_push_s2mPipe_m2sPipe.payload[1].outpData_1[1].myData;
  assign pushMidStm.payload[1].outpData_1[2].myData = io_push_s2mPipe_m2sPipe.payload[1].outpData_1[2].myData;
  assign pushMidStm.payload[2].inpData_0[0].myData = io_push_s2mPipe_m2sPipe.payload[2].inpData_0[0].myData;
  assign pushMidStm.payload[2].inpData_0[1].myData = io_push_s2mPipe_m2sPipe.payload[2].inpData_0[1].myData;
  assign pushMidStm.payload[2].inpData_0[2].myData = io_push_s2mPipe_m2sPipe.payload[2].inpData_0[2].myData;
  assign pushMidStm.payload[2].inpData_1[0].myData = io_push_s2mPipe_m2sPipe.payload[2].inpData_1[0].myData;
  assign pushMidStm.payload[2].inpData_1[1].myData = io_push_s2mPipe_m2sPipe.payload[2].inpData_1[1].myData;
  assign pushMidStm.payload[2].inpData_1[2].myData = io_push_s2mPipe_m2sPipe.payload[2].inpData_1[2].myData;
  assign pushMidStm.payload[2].outpData_0[0].myData = io_push_s2mPipe_m2sPipe.payload[2].outpData_0[0].myData;
  assign pushMidStm.payload[2].outpData_0[1].myData = io_push_s2mPipe_m2sPipe.payload[2].outpData_0[1].myData;
  assign pushMidStm.payload[2].outpData_0[2].myData = io_push_s2mPipe_m2sPipe.payload[2].outpData_0[2].myData;
  assign pushMidStm.payload[2].outpData_1[0].myData = io_push_s2mPipe_m2sPipe.payload[2].outpData_1[0].myData;
  assign pushMidStm.payload[2].outpData_1[1].myData = io_push_s2mPipe_m2sPipe.payload[2].outpData_1[1].myData;
  assign pushMidStm.payload[2].outpData_1[2].myData = io_push_s2mPipe_m2sPipe.payload[2].outpData_1[2].myData;
  assign popMidStm.ready = popMidStm_rValidN;
  assign popMidStm_s2mPipe.valid = (popMidStm.valid || (! popMidStm_rValidN));
  assign zzz_Interface_TestStreamPayloadInner_3.inpData_0[0].myData = (popMidStm_rValidN ? popMidStm.payload[0].inpData_0[0].myData : popMidStm_rData_0.inpData_0[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_3.inpData_0[1].myData = (popMidStm_rValidN ? popMidStm.payload[0].inpData_0[1].myData : popMidStm_rData_0.inpData_0[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_3.inpData_0[2].myData = (popMidStm_rValidN ? popMidStm.payload[0].inpData_0[2].myData : popMidStm_rData_0.inpData_0[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_3.inpData_1[0].myData = (popMidStm_rValidN ? popMidStm.payload[0].inpData_1[0].myData : popMidStm_rData_0.inpData_1[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_3.inpData_1[1].myData = (popMidStm_rValidN ? popMidStm.payload[0].inpData_1[1].myData : popMidStm_rData_0.inpData_1[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_3.inpData_1[2].myData = (popMidStm_rValidN ? popMidStm.payload[0].inpData_1[2].myData : popMidStm_rData_0.inpData_1[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_3.outpData_0[0].myData = (popMidStm_rValidN ? popMidStm.payload[0].outpData_0[0].myData : popMidStm_rData_0.outpData_0[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_3.outpData_0[1].myData = (popMidStm_rValidN ? popMidStm.payload[0].outpData_0[1].myData : popMidStm_rData_0.outpData_0[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_3.outpData_0[2].myData = (popMidStm_rValidN ? popMidStm.payload[0].outpData_0[2].myData : popMidStm_rData_0.outpData_0[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_3.outpData_1[0].myData = (popMidStm_rValidN ? popMidStm.payload[0].outpData_1[0].myData : popMidStm_rData_0.outpData_1[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_3.outpData_1[1].myData = (popMidStm_rValidN ? popMidStm.payload[0].outpData_1[1].myData : popMidStm_rData_0.outpData_1[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_3.outpData_1[2].myData = (popMidStm_rValidN ? popMidStm.payload[0].outpData_1[2].myData : popMidStm_rData_0.outpData_1[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_4.inpData_0[0].myData = (popMidStm_rValidN ? popMidStm.payload[1].inpData_0[0].myData : popMidStm_rData_1.inpData_0[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_4.inpData_0[1].myData = (popMidStm_rValidN ? popMidStm.payload[1].inpData_0[1].myData : popMidStm_rData_1.inpData_0[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_4.inpData_0[2].myData = (popMidStm_rValidN ? popMidStm.payload[1].inpData_0[2].myData : popMidStm_rData_1.inpData_0[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_4.inpData_1[0].myData = (popMidStm_rValidN ? popMidStm.payload[1].inpData_1[0].myData : popMidStm_rData_1.inpData_1[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_4.inpData_1[1].myData = (popMidStm_rValidN ? popMidStm.payload[1].inpData_1[1].myData : popMidStm_rData_1.inpData_1[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_4.inpData_1[2].myData = (popMidStm_rValidN ? popMidStm.payload[1].inpData_1[2].myData : popMidStm_rData_1.inpData_1[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_4.outpData_0[0].myData = (popMidStm_rValidN ? popMidStm.payload[1].outpData_0[0].myData : popMidStm_rData_1.outpData_0[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_4.outpData_0[1].myData = (popMidStm_rValidN ? popMidStm.payload[1].outpData_0[1].myData : popMidStm_rData_1.outpData_0[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_4.outpData_0[2].myData = (popMidStm_rValidN ? popMidStm.payload[1].outpData_0[2].myData : popMidStm_rData_1.outpData_0[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_4.outpData_1[0].myData = (popMidStm_rValidN ? popMidStm.payload[1].outpData_1[0].myData : popMidStm_rData_1.outpData_1[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_4.outpData_1[1].myData = (popMidStm_rValidN ? popMidStm.payload[1].outpData_1[1].myData : popMidStm_rData_1.outpData_1[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_4.outpData_1[2].myData = (popMidStm_rValidN ? popMidStm.payload[1].outpData_1[2].myData : popMidStm_rData_1.outpData_1[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_5.inpData_0[0].myData = (popMidStm_rValidN ? popMidStm.payload[2].inpData_0[0].myData : popMidStm_rData_2.inpData_0[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_5.inpData_0[1].myData = (popMidStm_rValidN ? popMidStm.payload[2].inpData_0[1].myData : popMidStm_rData_2.inpData_0[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_5.inpData_0[2].myData = (popMidStm_rValidN ? popMidStm.payload[2].inpData_0[2].myData : popMidStm_rData_2.inpData_0[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_5.inpData_1[0].myData = (popMidStm_rValidN ? popMidStm.payload[2].inpData_1[0].myData : popMidStm_rData_2.inpData_1[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_5.inpData_1[1].myData = (popMidStm_rValidN ? popMidStm.payload[2].inpData_1[1].myData : popMidStm_rData_2.inpData_1[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_5.inpData_1[2].myData = (popMidStm_rValidN ? popMidStm.payload[2].inpData_1[2].myData : popMidStm_rData_2.inpData_1[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_5.outpData_0[0].myData = (popMidStm_rValidN ? popMidStm.payload[2].outpData_0[0].myData : popMidStm_rData_2.outpData_0[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_5.outpData_0[1].myData = (popMidStm_rValidN ? popMidStm.payload[2].outpData_0[1].myData : popMidStm_rData_2.outpData_0[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_5.outpData_0[2].myData = (popMidStm_rValidN ? popMidStm.payload[2].outpData_0[2].myData : popMidStm_rData_2.outpData_0[2].myData);
  assign zzz_Interface_TestStreamPayloadInner_5.outpData_1[0].myData = (popMidStm_rValidN ? popMidStm.payload[2].outpData_1[0].myData : popMidStm_rData_2.outpData_1[0].myData);
  assign zzz_Interface_TestStreamPayloadInner_5.outpData_1[1].myData = (popMidStm_rValidN ? popMidStm.payload[2].outpData_1[1].myData : popMidStm_rData_2.outpData_1[1].myData);
  assign zzz_Interface_TestStreamPayloadInner_5.outpData_1[2].myData = (popMidStm_rValidN ? popMidStm.payload[2].outpData_1[2].myData : popMidStm_rData_2.outpData_1[2].myData);
  assign popMidStm_s2mPipe.payload[0].inpData_0[0].myData = zzz_Interface_TestStreamPayloadInner_3.inpData_0[0].myData;
  assign popMidStm_s2mPipe.payload[0].inpData_0[1].myData = zzz_Interface_TestStreamPayloadInner_3.inpData_0[1].myData;
  assign popMidStm_s2mPipe.payload[0].inpData_0[2].myData = zzz_Interface_TestStreamPayloadInner_3.inpData_0[2].myData;
  assign popMidStm_s2mPipe.payload[0].inpData_1[0].myData = zzz_Interface_TestStreamPayloadInner_3.inpData_1[0].myData;
  assign popMidStm_s2mPipe.payload[0].inpData_1[1].myData = zzz_Interface_TestStreamPayloadInner_3.inpData_1[1].myData;
  assign popMidStm_s2mPipe.payload[0].inpData_1[2].myData = zzz_Interface_TestStreamPayloadInner_3.inpData_1[2].myData;
  assign popMidStm_s2mPipe.payload[0].outpData_0[0].myData = zzz_Interface_TestStreamPayloadInner_3.outpData_0[0].myData;
  assign popMidStm_s2mPipe.payload[0].outpData_0[1].myData = zzz_Interface_TestStreamPayloadInner_3.outpData_0[1].myData;
  assign popMidStm_s2mPipe.payload[0].outpData_0[2].myData = zzz_Interface_TestStreamPayloadInner_3.outpData_0[2].myData;
  assign popMidStm_s2mPipe.payload[0].outpData_1[0].myData = zzz_Interface_TestStreamPayloadInner_3.outpData_1[0].myData;
  assign popMidStm_s2mPipe.payload[0].outpData_1[1].myData = zzz_Interface_TestStreamPayloadInner_3.outpData_1[1].myData;
  assign popMidStm_s2mPipe.payload[0].outpData_1[2].myData = zzz_Interface_TestStreamPayloadInner_3.outpData_1[2].myData;
  assign popMidStm_s2mPipe.payload[1].inpData_0[0].myData = zzz_Interface_TestStreamPayloadInner_4.inpData_0[0].myData;
  assign popMidStm_s2mPipe.payload[1].inpData_0[1].myData = zzz_Interface_TestStreamPayloadInner_4.inpData_0[1].myData;
  assign popMidStm_s2mPipe.payload[1].inpData_0[2].myData = zzz_Interface_TestStreamPayloadInner_4.inpData_0[2].myData;
  assign popMidStm_s2mPipe.payload[1].inpData_1[0].myData = zzz_Interface_TestStreamPayloadInner_4.inpData_1[0].myData;
  assign popMidStm_s2mPipe.payload[1].inpData_1[1].myData = zzz_Interface_TestStreamPayloadInner_4.inpData_1[1].myData;
  assign popMidStm_s2mPipe.payload[1].inpData_1[2].myData = zzz_Interface_TestStreamPayloadInner_4.inpData_1[2].myData;
  assign popMidStm_s2mPipe.payload[1].outpData_0[0].myData = zzz_Interface_TestStreamPayloadInner_4.outpData_0[0].myData;
  assign popMidStm_s2mPipe.payload[1].outpData_0[1].myData = zzz_Interface_TestStreamPayloadInner_4.outpData_0[1].myData;
  assign popMidStm_s2mPipe.payload[1].outpData_0[2].myData = zzz_Interface_TestStreamPayloadInner_4.outpData_0[2].myData;
  assign popMidStm_s2mPipe.payload[1].outpData_1[0].myData = zzz_Interface_TestStreamPayloadInner_4.outpData_1[0].myData;
  assign popMidStm_s2mPipe.payload[1].outpData_1[1].myData = zzz_Interface_TestStreamPayloadInner_4.outpData_1[1].myData;
  assign popMidStm_s2mPipe.payload[1].outpData_1[2].myData = zzz_Interface_TestStreamPayloadInner_4.outpData_1[2].myData;
  assign popMidStm_s2mPipe.payload[2].inpData_0[0].myData = zzz_Interface_TestStreamPayloadInner_5.inpData_0[0].myData;
  assign popMidStm_s2mPipe.payload[2].inpData_0[1].myData = zzz_Interface_TestStreamPayloadInner_5.inpData_0[1].myData;
  assign popMidStm_s2mPipe.payload[2].inpData_0[2].myData = zzz_Interface_TestStreamPayloadInner_5.inpData_0[2].myData;
  assign popMidStm_s2mPipe.payload[2].inpData_1[0].myData = zzz_Interface_TestStreamPayloadInner_5.inpData_1[0].myData;
  assign popMidStm_s2mPipe.payload[2].inpData_1[1].myData = zzz_Interface_TestStreamPayloadInner_5.inpData_1[1].myData;
  assign popMidStm_s2mPipe.payload[2].inpData_1[2].myData = zzz_Interface_TestStreamPayloadInner_5.inpData_1[2].myData;
  assign popMidStm_s2mPipe.payload[2].outpData_0[0].myData = zzz_Interface_TestStreamPayloadInner_5.outpData_0[0].myData;
  assign popMidStm_s2mPipe.payload[2].outpData_0[1].myData = zzz_Interface_TestStreamPayloadInner_5.outpData_0[1].myData;
  assign popMidStm_s2mPipe.payload[2].outpData_0[2].myData = zzz_Interface_TestStreamPayloadInner_5.outpData_0[2].myData;
  assign popMidStm_s2mPipe.payload[2].outpData_1[0].myData = zzz_Interface_TestStreamPayloadInner_5.outpData_1[0].myData;
  assign popMidStm_s2mPipe.payload[2].outpData_1[1].myData = zzz_Interface_TestStreamPayloadInner_5.outpData_1[1].myData;
  assign popMidStm_s2mPipe.payload[2].outpData_1[2].myData = zzz_Interface_TestStreamPayloadInner_5.outpData_1[2].myData;
  always @(*) begin
    popMidStm_s2mPipe.ready = popMidStm_s2mPipe_m2sPipe.ready;
    if(when_Stream_l401_1) begin
      popMidStm_s2mPipe.ready = 1'b1;
    end
  end

  assign when_Stream_l401_1 = (! popMidStm_s2mPipe_m2sPipe.valid);
  assign popMidStm_s2mPipe_m2sPipe.valid = popMidStm_s2mPipe_rValid;
  assign popMidStm_s2mPipe_m2sPipe.payload[0].inpData_0[0].myData = popMidStm_s2mPipe_rData_0.inpData_0[0].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[0].inpData_0[1].myData = popMidStm_s2mPipe_rData_0.inpData_0[1].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[0].inpData_0[2].myData = popMidStm_s2mPipe_rData_0.inpData_0[2].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[0].inpData_1[0].myData = popMidStm_s2mPipe_rData_0.inpData_1[0].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[0].inpData_1[1].myData = popMidStm_s2mPipe_rData_0.inpData_1[1].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[0].inpData_1[2].myData = popMidStm_s2mPipe_rData_0.inpData_1[2].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[0].outpData_0[0].myData = popMidStm_s2mPipe_rData_0.outpData_0[0].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[0].outpData_0[1].myData = popMidStm_s2mPipe_rData_0.outpData_0[1].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[0].outpData_0[2].myData = popMidStm_s2mPipe_rData_0.outpData_0[2].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[0].outpData_1[0].myData = popMidStm_s2mPipe_rData_0.outpData_1[0].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[0].outpData_1[1].myData = popMidStm_s2mPipe_rData_0.outpData_1[1].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[0].outpData_1[2].myData = popMidStm_s2mPipe_rData_0.outpData_1[2].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[1].inpData_0[0].myData = popMidStm_s2mPipe_rData_1.inpData_0[0].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[1].inpData_0[1].myData = popMidStm_s2mPipe_rData_1.inpData_0[1].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[1].inpData_0[2].myData = popMidStm_s2mPipe_rData_1.inpData_0[2].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[1].inpData_1[0].myData = popMidStm_s2mPipe_rData_1.inpData_1[0].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[1].inpData_1[1].myData = popMidStm_s2mPipe_rData_1.inpData_1[1].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[1].inpData_1[2].myData = popMidStm_s2mPipe_rData_1.inpData_1[2].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[1].outpData_0[0].myData = popMidStm_s2mPipe_rData_1.outpData_0[0].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[1].outpData_0[1].myData = popMidStm_s2mPipe_rData_1.outpData_0[1].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[1].outpData_0[2].myData = popMidStm_s2mPipe_rData_1.outpData_0[2].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[1].outpData_1[0].myData = popMidStm_s2mPipe_rData_1.outpData_1[0].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[1].outpData_1[1].myData = popMidStm_s2mPipe_rData_1.outpData_1[1].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[1].outpData_1[2].myData = popMidStm_s2mPipe_rData_1.outpData_1[2].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[2].inpData_0[0].myData = popMidStm_s2mPipe_rData_2.inpData_0[0].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[2].inpData_0[1].myData = popMidStm_s2mPipe_rData_2.inpData_0[1].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[2].inpData_0[2].myData = popMidStm_s2mPipe_rData_2.inpData_0[2].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[2].inpData_1[0].myData = popMidStm_s2mPipe_rData_2.inpData_1[0].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[2].inpData_1[1].myData = popMidStm_s2mPipe_rData_2.inpData_1[1].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[2].inpData_1[2].myData = popMidStm_s2mPipe_rData_2.inpData_1[2].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[2].outpData_0[0].myData = popMidStm_s2mPipe_rData_2.outpData_0[0].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[2].outpData_0[1].myData = popMidStm_s2mPipe_rData_2.outpData_0[1].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[2].outpData_0[2].myData = popMidStm_s2mPipe_rData_2.outpData_0[2].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[2].outpData_1[0].myData = popMidStm_s2mPipe_rData_2.outpData_1[0].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[2].outpData_1[1].myData = popMidStm_s2mPipe_rData_2.outpData_1[1].myData;
  assign popMidStm_s2mPipe_m2sPipe.payload[2].outpData_1[2].myData = popMidStm_s2mPipe_rData_2.outpData_1[2].myData;
  assign io.pop.valid = popMidStm_s2mPipe_m2sPipe.valid;
  assign popMidStm_s2mPipe_m2sPipe.ready = io.pop.ready;
  assign io.pop.payload[0].inpData_0[0].myData = popMidStm_s2mPipe_m2sPipe.payload[0].inpData_0[0].myData;
  assign io.pop.payload[0].inpData_0[1].myData = popMidStm_s2mPipe_m2sPipe.payload[0].inpData_0[1].myData;
  assign io.pop.payload[0].inpData_0[2].myData = popMidStm_s2mPipe_m2sPipe.payload[0].inpData_0[2].myData;
  assign io.pop.payload[0].inpData_1[0].myData = popMidStm_s2mPipe_m2sPipe.payload[0].inpData_1[0].myData;
  assign io.pop.payload[0].inpData_1[1].myData = popMidStm_s2mPipe_m2sPipe.payload[0].inpData_1[1].myData;
  assign io.pop.payload[0].inpData_1[2].myData = popMidStm_s2mPipe_m2sPipe.payload[0].inpData_1[2].myData;
  assign io.pop.payload[0].outpData_0[0].myData = popMidStm_s2mPipe_m2sPipe.payload[0].outpData_0[0].myData;
  assign io.pop.payload[0].outpData_0[1].myData = popMidStm_s2mPipe_m2sPipe.payload[0].outpData_0[1].myData;
  assign io.pop.payload[0].outpData_0[2].myData = popMidStm_s2mPipe_m2sPipe.payload[0].outpData_0[2].myData;
  assign io.pop.payload[0].outpData_1[0].myData = popMidStm_s2mPipe_m2sPipe.payload[0].outpData_1[0].myData;
  assign io.pop.payload[0].outpData_1[1].myData = popMidStm_s2mPipe_m2sPipe.payload[0].outpData_1[1].myData;
  assign io.pop.payload[0].outpData_1[2].myData = popMidStm_s2mPipe_m2sPipe.payload[0].outpData_1[2].myData;
  assign io.pop.payload[1].inpData_0[0].myData = popMidStm_s2mPipe_m2sPipe.payload[1].inpData_0[0].myData;
  assign io.pop.payload[1].inpData_0[1].myData = popMidStm_s2mPipe_m2sPipe.payload[1].inpData_0[1].myData;
  assign io.pop.payload[1].inpData_0[2].myData = popMidStm_s2mPipe_m2sPipe.payload[1].inpData_0[2].myData;
  assign io.pop.payload[1].inpData_1[0].myData = popMidStm_s2mPipe_m2sPipe.payload[1].inpData_1[0].myData;
  assign io.pop.payload[1].inpData_1[1].myData = popMidStm_s2mPipe_m2sPipe.payload[1].inpData_1[1].myData;
  assign io.pop.payload[1].inpData_1[2].myData = popMidStm_s2mPipe_m2sPipe.payload[1].inpData_1[2].myData;
  assign io.pop.payload[1].outpData_0[0].myData = popMidStm_s2mPipe_m2sPipe.payload[1].outpData_0[0].myData;
  assign io.pop.payload[1].outpData_0[1].myData = popMidStm_s2mPipe_m2sPipe.payload[1].outpData_0[1].myData;
  assign io.pop.payload[1].outpData_0[2].myData = popMidStm_s2mPipe_m2sPipe.payload[1].outpData_0[2].myData;
  assign io.pop.payload[1].outpData_1[0].myData = popMidStm_s2mPipe_m2sPipe.payload[1].outpData_1[0].myData;
  assign io.pop.payload[1].outpData_1[1].myData = popMidStm_s2mPipe_m2sPipe.payload[1].outpData_1[1].myData;
  assign io.pop.payload[1].outpData_1[2].myData = popMidStm_s2mPipe_m2sPipe.payload[1].outpData_1[2].myData;
  assign io.pop.payload[2].inpData_0[0].myData = popMidStm_s2mPipe_m2sPipe.payload[2].inpData_0[0].myData;
  assign io.pop.payload[2].inpData_0[1].myData = popMidStm_s2mPipe_m2sPipe.payload[2].inpData_0[1].myData;
  assign io.pop.payload[2].inpData_0[2].myData = popMidStm_s2mPipe_m2sPipe.payload[2].inpData_0[2].myData;
  assign io.pop.payload[2].inpData_1[0].myData = popMidStm_s2mPipe_m2sPipe.payload[2].inpData_1[0].myData;
  assign io.pop.payload[2].inpData_1[1].myData = popMidStm_s2mPipe_m2sPipe.payload[2].inpData_1[1].myData;
  assign io.pop.payload[2].inpData_1[2].myData = popMidStm_s2mPipe_m2sPipe.payload[2].inpData_1[2].myData;
  assign io.pop.payload[2].outpData_0[0].myData = popMidStm_s2mPipe_m2sPipe.payload[2].outpData_0[0].myData;
  assign io.pop.payload[2].outpData_0[1].myData = popMidStm_s2mPipe_m2sPipe.payload[2].outpData_0[1].myData;
  assign io.pop.payload[2].outpData_0[2].myData = popMidStm_s2mPipe_m2sPipe.payload[2].outpData_0[2].myData;
  assign io.pop.payload[2].outpData_1[0].myData = popMidStm_s2mPipe_m2sPipe.payload[2].outpData_1[0].myData;
  assign io.pop.payload[2].outpData_1[1].myData = popMidStm_s2mPipe_m2sPipe.payload[2].outpData_1[1].myData;
  assign io.pop.payload[2].outpData_1[2].myData = popMidStm_s2mPipe_m2sPipe.payload[2].outpData_1[2].myData;
  assign popMidStm.valid = pushMidStm.valid;
  assign pushMidStm.ready = popMidStm.ready;
  assign popMidStm.payload[0].inpData_0[0].myData = pushMidStm.payload[0].inpData_0[0].myData;
  assign popMidStm.payload[0].outpData_0[0].myData = (pushMidStm.payload[0].inpData_0[0].myData + 8'h01);
  assign popMidStm.payload[0].inpData_0[1].myData = pushMidStm.payload[0].inpData_0[1].myData;
  assign popMidStm.payload[0].outpData_0[1].myData = (pushMidStm.payload[0].inpData_0[1].myData + 8'h01);
  assign popMidStm.payload[0].inpData_0[2].myData = pushMidStm.payload[0].inpData_0[2].myData;
  assign popMidStm.payload[0].outpData_0[2].myData = (pushMidStm.payload[0].inpData_0[2].myData + 8'h01);
  assign popMidStm.payload[0].inpData_1[0].myData = pushMidStm.payload[0].inpData_1[0].myData;
  assign popMidStm.payload[0].outpData_1[0].myData = (pushMidStm.payload[0].inpData_1[0].myData + 8'h01);
  assign popMidStm.payload[0].inpData_1[1].myData = pushMidStm.payload[0].inpData_1[1].myData;
  assign popMidStm.payload[0].outpData_1[1].myData = (pushMidStm.payload[0].inpData_1[1].myData + 8'h01);
  assign popMidStm.payload[0].inpData_1[2].myData = pushMidStm.payload[0].inpData_1[2].myData;
  assign popMidStm.payload[0].outpData_1[2].myData = (pushMidStm.payload[0].inpData_1[2].myData + 8'h01);
  assign popMidStm.payload[1].inpData_0[0].myData = pushMidStm.payload[1].inpData_0[0].myData;
  assign popMidStm.payload[1].outpData_0[0].myData = (pushMidStm.payload[1].inpData_0[0].myData + 8'h01);
  assign popMidStm.payload[1].inpData_0[1].myData = pushMidStm.payload[1].inpData_0[1].myData;
  assign popMidStm.payload[1].outpData_0[1].myData = (pushMidStm.payload[1].inpData_0[1].myData + 8'h01);
  assign popMidStm.payload[1].inpData_0[2].myData = pushMidStm.payload[1].inpData_0[2].myData;
  assign popMidStm.payload[1].outpData_0[2].myData = (pushMidStm.payload[1].inpData_0[2].myData + 8'h01);
  assign popMidStm.payload[1].inpData_1[0].myData = pushMidStm.payload[1].inpData_1[0].myData;
  assign popMidStm.payload[1].outpData_1[0].myData = (pushMidStm.payload[1].inpData_1[0].myData + 8'h01);
  assign popMidStm.payload[1].inpData_1[1].myData = pushMidStm.payload[1].inpData_1[1].myData;
  assign popMidStm.payload[1].outpData_1[1].myData = (pushMidStm.payload[1].inpData_1[1].myData + 8'h01);
  assign popMidStm.payload[1].inpData_1[2].myData = pushMidStm.payload[1].inpData_1[2].myData;
  assign popMidStm.payload[1].outpData_1[2].myData = (pushMidStm.payload[1].inpData_1[2].myData + 8'h01);
  assign popMidStm.payload[2].inpData_0[0].myData = pushMidStm.payload[2].inpData_0[0].myData;
  assign popMidStm.payload[2].outpData_0[0].myData = (pushMidStm.payload[2].inpData_0[0].myData + 8'h01);
  assign popMidStm.payload[2].inpData_0[1].myData = pushMidStm.payload[2].inpData_0[1].myData;
  assign popMidStm.payload[2].outpData_0[1].myData = (pushMidStm.payload[2].inpData_0[1].myData + 8'h01);
  assign popMidStm.payload[2].inpData_0[2].myData = pushMidStm.payload[2].inpData_0[2].myData;
  assign popMidStm.payload[2].outpData_0[2].myData = (pushMidStm.payload[2].inpData_0[2].myData + 8'h01);
  assign popMidStm.payload[2].inpData_1[0].myData = pushMidStm.payload[2].inpData_1[0].myData;
  assign popMidStm.payload[2].outpData_1[0].myData = (pushMidStm.payload[2].inpData_1[0].myData + 8'h01);
  assign popMidStm.payload[2].inpData_1[1].myData = pushMidStm.payload[2].inpData_1[1].myData;
  assign popMidStm.payload[2].outpData_1[1].myData = (pushMidStm.payload[2].inpData_1[1].myData + 8'h01);
  assign popMidStm.payload[2].inpData_1[2].myData = pushMidStm.payload[2].inpData_1[2].myData;
  assign popMidStm.payload[2].outpData_1[2].myData = (pushMidStm.payload[2].inpData_1[2].myData + 8'h01);
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      io_push_rValidN <= 1'b1;
      io_push_s2mPipe_rValid <= 1'b0;
      popMidStm_rValidN <= 1'b1;
      popMidStm_s2mPipe_rValid <= 1'b0;
    end else begin
      if(io.push.valid) begin
        io_push_rValidN <= 1'b0;
      end
      if(io_push_s2mPipe.ready) begin
        io_push_rValidN <= 1'b1;
      end
      if(io_push_s2mPipe.ready) begin
        io_push_s2mPipe_rValid <= io_push_s2mPipe.valid;
      end
      if(popMidStm.valid) begin
        popMidStm_rValidN <= 1'b0;
      end
      if(popMidStm_s2mPipe.ready) begin
        popMidStm_rValidN <= 1'b1;
      end
      if(popMidStm_s2mPipe.ready) begin
        popMidStm_s2mPipe_rValid <= popMidStm_s2mPipe.valid;
      end
    end
  end

  always @(posedge clk) begin
    if(io.push.ready) begin
      io_push_rData_0.inpData_0[0].myData <= io.push.payload[0].inpData_0[0].myData;
      io_push_rData_0.inpData_0[1].myData <= io.push.payload[0].inpData_0[1].myData;
      io_push_rData_0.inpData_0[2].myData <= io.push.payload[0].inpData_0[2].myData;
      io_push_rData_0.inpData_1[0].myData <= io.push.payload[0].inpData_1[0].myData;
      io_push_rData_0.inpData_1[1].myData <= io.push.payload[0].inpData_1[1].myData;
      io_push_rData_0.inpData_1[2].myData <= io.push.payload[0].inpData_1[2].myData;
      io_push_rData_0.outpData_0[0].myData <= io.push.payload[0].outpData_0[0].myData;
      io_push_rData_0.outpData_0[1].myData <= io.push.payload[0].outpData_0[1].myData;
      io_push_rData_0.outpData_0[2].myData <= io.push.payload[0].outpData_0[2].myData;
      io_push_rData_0.outpData_1[0].myData <= io.push.payload[0].outpData_1[0].myData;
      io_push_rData_0.outpData_1[1].myData <= io.push.payload[0].outpData_1[1].myData;
      io_push_rData_0.outpData_1[2].myData <= io.push.payload[0].outpData_1[2].myData;
      io_push_rData_1.inpData_0[0].myData <= io.push.payload[1].inpData_0[0].myData;
      io_push_rData_1.inpData_0[1].myData <= io.push.payload[1].inpData_0[1].myData;
      io_push_rData_1.inpData_0[2].myData <= io.push.payload[1].inpData_0[2].myData;
      io_push_rData_1.inpData_1[0].myData <= io.push.payload[1].inpData_1[0].myData;
      io_push_rData_1.inpData_1[1].myData <= io.push.payload[1].inpData_1[1].myData;
      io_push_rData_1.inpData_1[2].myData <= io.push.payload[1].inpData_1[2].myData;
      io_push_rData_1.outpData_0[0].myData <= io.push.payload[1].outpData_0[0].myData;
      io_push_rData_1.outpData_0[1].myData <= io.push.payload[1].outpData_0[1].myData;
      io_push_rData_1.outpData_0[2].myData <= io.push.payload[1].outpData_0[2].myData;
      io_push_rData_1.outpData_1[0].myData <= io.push.payload[1].outpData_1[0].myData;
      io_push_rData_1.outpData_1[1].myData <= io.push.payload[1].outpData_1[1].myData;
      io_push_rData_1.outpData_1[2].myData <= io.push.payload[1].outpData_1[2].myData;
      io_push_rData_2.inpData_0[0].myData <= io.push.payload[2].inpData_0[0].myData;
      io_push_rData_2.inpData_0[1].myData <= io.push.payload[2].inpData_0[1].myData;
      io_push_rData_2.inpData_0[2].myData <= io.push.payload[2].inpData_0[2].myData;
      io_push_rData_2.inpData_1[0].myData <= io.push.payload[2].inpData_1[0].myData;
      io_push_rData_2.inpData_1[1].myData <= io.push.payload[2].inpData_1[1].myData;
      io_push_rData_2.inpData_1[2].myData <= io.push.payload[2].inpData_1[2].myData;
      io_push_rData_2.outpData_0[0].myData <= io.push.payload[2].outpData_0[0].myData;
      io_push_rData_2.outpData_0[1].myData <= io.push.payload[2].outpData_0[1].myData;
      io_push_rData_2.outpData_0[2].myData <= io.push.payload[2].outpData_0[2].myData;
      io_push_rData_2.outpData_1[0].myData <= io.push.payload[2].outpData_1[0].myData;
      io_push_rData_2.outpData_1[1].myData <= io.push.payload[2].outpData_1[1].myData;
      io_push_rData_2.outpData_1[2].myData <= io.push.payload[2].outpData_1[2].myData;
    end
    if(io_push_s2mPipe.ready) begin
      io_push_s2mPipe_rData_0.inpData_0[0].myData <= io_push_s2mPipe.payload[0].inpData_0[0].myData;
      io_push_s2mPipe_rData_0.inpData_0[1].myData <= io_push_s2mPipe.payload[0].inpData_0[1].myData;
      io_push_s2mPipe_rData_0.inpData_0[2].myData <= io_push_s2mPipe.payload[0].inpData_0[2].myData;
      io_push_s2mPipe_rData_0.inpData_1[0].myData <= io_push_s2mPipe.payload[0].inpData_1[0].myData;
      io_push_s2mPipe_rData_0.inpData_1[1].myData <= io_push_s2mPipe.payload[0].inpData_1[1].myData;
      io_push_s2mPipe_rData_0.inpData_1[2].myData <= io_push_s2mPipe.payload[0].inpData_1[2].myData;
      io_push_s2mPipe_rData_0.outpData_0[0].myData <= io_push_s2mPipe.payload[0].outpData_0[0].myData;
      io_push_s2mPipe_rData_0.outpData_0[1].myData <= io_push_s2mPipe.payload[0].outpData_0[1].myData;
      io_push_s2mPipe_rData_0.outpData_0[2].myData <= io_push_s2mPipe.payload[0].outpData_0[2].myData;
      io_push_s2mPipe_rData_0.outpData_1[0].myData <= io_push_s2mPipe.payload[0].outpData_1[0].myData;
      io_push_s2mPipe_rData_0.outpData_1[1].myData <= io_push_s2mPipe.payload[0].outpData_1[1].myData;
      io_push_s2mPipe_rData_0.outpData_1[2].myData <= io_push_s2mPipe.payload[0].outpData_1[2].myData;
      io_push_s2mPipe_rData_1.inpData_0[0].myData <= io_push_s2mPipe.payload[1].inpData_0[0].myData;
      io_push_s2mPipe_rData_1.inpData_0[1].myData <= io_push_s2mPipe.payload[1].inpData_0[1].myData;
      io_push_s2mPipe_rData_1.inpData_0[2].myData <= io_push_s2mPipe.payload[1].inpData_0[2].myData;
      io_push_s2mPipe_rData_1.inpData_1[0].myData <= io_push_s2mPipe.payload[1].inpData_1[0].myData;
      io_push_s2mPipe_rData_1.inpData_1[1].myData <= io_push_s2mPipe.payload[1].inpData_1[1].myData;
      io_push_s2mPipe_rData_1.inpData_1[2].myData <= io_push_s2mPipe.payload[1].inpData_1[2].myData;
      io_push_s2mPipe_rData_1.outpData_0[0].myData <= io_push_s2mPipe.payload[1].outpData_0[0].myData;
      io_push_s2mPipe_rData_1.outpData_0[1].myData <= io_push_s2mPipe.payload[1].outpData_0[1].myData;
      io_push_s2mPipe_rData_1.outpData_0[2].myData <= io_push_s2mPipe.payload[1].outpData_0[2].myData;
      io_push_s2mPipe_rData_1.outpData_1[0].myData <= io_push_s2mPipe.payload[1].outpData_1[0].myData;
      io_push_s2mPipe_rData_1.outpData_1[1].myData <= io_push_s2mPipe.payload[1].outpData_1[1].myData;
      io_push_s2mPipe_rData_1.outpData_1[2].myData <= io_push_s2mPipe.payload[1].outpData_1[2].myData;
      io_push_s2mPipe_rData_2.inpData_0[0].myData <= io_push_s2mPipe.payload[2].inpData_0[0].myData;
      io_push_s2mPipe_rData_2.inpData_0[1].myData <= io_push_s2mPipe.payload[2].inpData_0[1].myData;
      io_push_s2mPipe_rData_2.inpData_0[2].myData <= io_push_s2mPipe.payload[2].inpData_0[2].myData;
      io_push_s2mPipe_rData_2.inpData_1[0].myData <= io_push_s2mPipe.payload[2].inpData_1[0].myData;
      io_push_s2mPipe_rData_2.inpData_1[1].myData <= io_push_s2mPipe.payload[2].inpData_1[1].myData;
      io_push_s2mPipe_rData_2.inpData_1[2].myData <= io_push_s2mPipe.payload[2].inpData_1[2].myData;
      io_push_s2mPipe_rData_2.outpData_0[0].myData <= io_push_s2mPipe.payload[2].outpData_0[0].myData;
      io_push_s2mPipe_rData_2.outpData_0[1].myData <= io_push_s2mPipe.payload[2].outpData_0[1].myData;
      io_push_s2mPipe_rData_2.outpData_0[2].myData <= io_push_s2mPipe.payload[2].outpData_0[2].myData;
      io_push_s2mPipe_rData_2.outpData_1[0].myData <= io_push_s2mPipe.payload[2].outpData_1[0].myData;
      io_push_s2mPipe_rData_2.outpData_1[1].myData <= io_push_s2mPipe.payload[2].outpData_1[1].myData;
      io_push_s2mPipe_rData_2.outpData_1[2].myData <= io_push_s2mPipe.payload[2].outpData_1[2].myData;
    end
    if(popMidStm.ready) begin
      popMidStm_rData_0.inpData_0[0].myData <= popMidStm.payload[0].inpData_0[0].myData;
      popMidStm_rData_0.inpData_0[1].myData <= popMidStm.payload[0].inpData_0[1].myData;
      popMidStm_rData_0.inpData_0[2].myData <= popMidStm.payload[0].inpData_0[2].myData;
      popMidStm_rData_0.inpData_1[0].myData <= popMidStm.payload[0].inpData_1[0].myData;
      popMidStm_rData_0.inpData_1[1].myData <= popMidStm.payload[0].inpData_1[1].myData;
      popMidStm_rData_0.inpData_1[2].myData <= popMidStm.payload[0].inpData_1[2].myData;
      popMidStm_rData_0.outpData_0[0].myData <= popMidStm.payload[0].outpData_0[0].myData;
      popMidStm_rData_0.outpData_0[1].myData <= popMidStm.payload[0].outpData_0[1].myData;
      popMidStm_rData_0.outpData_0[2].myData <= popMidStm.payload[0].outpData_0[2].myData;
      popMidStm_rData_0.outpData_1[0].myData <= popMidStm.payload[0].outpData_1[0].myData;
      popMidStm_rData_0.outpData_1[1].myData <= popMidStm.payload[0].outpData_1[1].myData;
      popMidStm_rData_0.outpData_1[2].myData <= popMidStm.payload[0].outpData_1[2].myData;
      popMidStm_rData_1.inpData_0[0].myData <= popMidStm.payload[1].inpData_0[0].myData;
      popMidStm_rData_1.inpData_0[1].myData <= popMidStm.payload[1].inpData_0[1].myData;
      popMidStm_rData_1.inpData_0[2].myData <= popMidStm.payload[1].inpData_0[2].myData;
      popMidStm_rData_1.inpData_1[0].myData <= popMidStm.payload[1].inpData_1[0].myData;
      popMidStm_rData_1.inpData_1[1].myData <= popMidStm.payload[1].inpData_1[1].myData;
      popMidStm_rData_1.inpData_1[2].myData <= popMidStm.payload[1].inpData_1[2].myData;
      popMidStm_rData_1.outpData_0[0].myData <= popMidStm.payload[1].outpData_0[0].myData;
      popMidStm_rData_1.outpData_0[1].myData <= popMidStm.payload[1].outpData_0[1].myData;
      popMidStm_rData_1.outpData_0[2].myData <= popMidStm.payload[1].outpData_0[2].myData;
      popMidStm_rData_1.outpData_1[0].myData <= popMidStm.payload[1].outpData_1[0].myData;
      popMidStm_rData_1.outpData_1[1].myData <= popMidStm.payload[1].outpData_1[1].myData;
      popMidStm_rData_1.outpData_1[2].myData <= popMidStm.payload[1].outpData_1[2].myData;
      popMidStm_rData_2.inpData_0[0].myData <= popMidStm.payload[2].inpData_0[0].myData;
      popMidStm_rData_2.inpData_0[1].myData <= popMidStm.payload[2].inpData_0[1].myData;
      popMidStm_rData_2.inpData_0[2].myData <= popMidStm.payload[2].inpData_0[2].myData;
      popMidStm_rData_2.inpData_1[0].myData <= popMidStm.payload[2].inpData_1[0].myData;
      popMidStm_rData_2.inpData_1[1].myData <= popMidStm.payload[2].inpData_1[1].myData;
      popMidStm_rData_2.inpData_1[2].myData <= popMidStm.payload[2].inpData_1[2].myData;
      popMidStm_rData_2.outpData_0[0].myData <= popMidStm.payload[2].outpData_0[0].myData;
      popMidStm_rData_2.outpData_0[1].myData <= popMidStm.payload[2].outpData_0[1].myData;
      popMidStm_rData_2.outpData_0[2].myData <= popMidStm.payload[2].outpData_0[2].myData;
      popMidStm_rData_2.outpData_1[0].myData <= popMidStm.payload[2].outpData_1[0].myData;
      popMidStm_rData_2.outpData_1[1].myData <= popMidStm.payload[2].outpData_1[1].myData;
      popMidStm_rData_2.outpData_1[2].myData <= popMidStm.payload[2].outpData_1[2].myData;
    end
    if(popMidStm_s2mPipe.ready) begin
      popMidStm_s2mPipe_rData_0.inpData_0[0].myData <= popMidStm_s2mPipe.payload[0].inpData_0[0].myData;
      popMidStm_s2mPipe_rData_0.inpData_0[1].myData <= popMidStm_s2mPipe.payload[0].inpData_0[1].myData;
      popMidStm_s2mPipe_rData_0.inpData_0[2].myData <= popMidStm_s2mPipe.payload[0].inpData_0[2].myData;
      popMidStm_s2mPipe_rData_0.inpData_1[0].myData <= popMidStm_s2mPipe.payload[0].inpData_1[0].myData;
      popMidStm_s2mPipe_rData_0.inpData_1[1].myData <= popMidStm_s2mPipe.payload[0].inpData_1[1].myData;
      popMidStm_s2mPipe_rData_0.inpData_1[2].myData <= popMidStm_s2mPipe.payload[0].inpData_1[2].myData;
      popMidStm_s2mPipe_rData_0.outpData_0[0].myData <= popMidStm_s2mPipe.payload[0].outpData_0[0].myData;
      popMidStm_s2mPipe_rData_0.outpData_0[1].myData <= popMidStm_s2mPipe.payload[0].outpData_0[1].myData;
      popMidStm_s2mPipe_rData_0.outpData_0[2].myData <= popMidStm_s2mPipe.payload[0].outpData_0[2].myData;
      popMidStm_s2mPipe_rData_0.outpData_1[0].myData <= popMidStm_s2mPipe.payload[0].outpData_1[0].myData;
      popMidStm_s2mPipe_rData_0.outpData_1[1].myData <= popMidStm_s2mPipe.payload[0].outpData_1[1].myData;
      popMidStm_s2mPipe_rData_0.outpData_1[2].myData <= popMidStm_s2mPipe.payload[0].outpData_1[2].myData;
      popMidStm_s2mPipe_rData_1.inpData_0[0].myData <= popMidStm_s2mPipe.payload[1].inpData_0[0].myData;
      popMidStm_s2mPipe_rData_1.inpData_0[1].myData <= popMidStm_s2mPipe.payload[1].inpData_0[1].myData;
      popMidStm_s2mPipe_rData_1.inpData_0[2].myData <= popMidStm_s2mPipe.payload[1].inpData_0[2].myData;
      popMidStm_s2mPipe_rData_1.inpData_1[0].myData <= popMidStm_s2mPipe.payload[1].inpData_1[0].myData;
      popMidStm_s2mPipe_rData_1.inpData_1[1].myData <= popMidStm_s2mPipe.payload[1].inpData_1[1].myData;
      popMidStm_s2mPipe_rData_1.inpData_1[2].myData <= popMidStm_s2mPipe.payload[1].inpData_1[2].myData;
      popMidStm_s2mPipe_rData_1.outpData_0[0].myData <= popMidStm_s2mPipe.payload[1].outpData_0[0].myData;
      popMidStm_s2mPipe_rData_1.outpData_0[1].myData <= popMidStm_s2mPipe.payload[1].outpData_0[1].myData;
      popMidStm_s2mPipe_rData_1.outpData_0[2].myData <= popMidStm_s2mPipe.payload[1].outpData_0[2].myData;
      popMidStm_s2mPipe_rData_1.outpData_1[0].myData <= popMidStm_s2mPipe.payload[1].outpData_1[0].myData;
      popMidStm_s2mPipe_rData_1.outpData_1[1].myData <= popMidStm_s2mPipe.payload[1].outpData_1[1].myData;
      popMidStm_s2mPipe_rData_1.outpData_1[2].myData <= popMidStm_s2mPipe.payload[1].outpData_1[2].myData;
      popMidStm_s2mPipe_rData_2.inpData_0[0].myData <= popMidStm_s2mPipe.payload[2].inpData_0[0].myData;
      popMidStm_s2mPipe_rData_2.inpData_0[1].myData <= popMidStm_s2mPipe.payload[2].inpData_0[1].myData;
      popMidStm_s2mPipe_rData_2.inpData_0[2].myData <= popMidStm_s2mPipe.payload[2].inpData_0[2].myData;
      popMidStm_s2mPipe_rData_2.inpData_1[0].myData <= popMidStm_s2mPipe.payload[2].inpData_1[0].myData;
      popMidStm_s2mPipe_rData_2.inpData_1[1].myData <= popMidStm_s2mPipe.payload[2].inpData_1[1].myData;
      popMidStm_s2mPipe_rData_2.inpData_1[2].myData <= popMidStm_s2mPipe.payload[2].inpData_1[2].myData;
      popMidStm_s2mPipe_rData_2.outpData_0[0].myData <= popMidStm_s2mPipe.payload[2].outpData_0[0].myData;
      popMidStm_s2mPipe_rData_2.outpData_0[1].myData <= popMidStm_s2mPipe.payload[2].outpData_0[1].myData;
      popMidStm_s2mPipe_rData_2.outpData_0[2].myData <= popMidStm_s2mPipe.payload[2].outpData_0[2].myData;
      popMidStm_s2mPipe_rData_2.outpData_1[0].myData <= popMidStm_s2mPipe.payload[2].outpData_1[0].myData;
      popMidStm_s2mPipe_rData_2.outpData_1[1].myData <= popMidStm_s2mPipe.payload[2].outpData_1[1].myData;
      popMidStm_s2mPipe_rData_2.outpData_1[2].myData <= popMidStm_s2mPipe.payload[2].outpData_1[2].myData;
    end
  end


endmodule
