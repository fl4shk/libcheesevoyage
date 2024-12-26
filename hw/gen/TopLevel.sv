// Generator : SpinalHDL dev    git head : e7a32ed060dbfabadca7a827d45277b2ff5e2dca
// Component : TopLevel
// Git hash  : 95bb5dce5312c656b29e60cc48fe159315e3c5c7

`timescale 1ns/1ps

typedef struct {

  logic  [7:0]    myData ;

} TestStreamPayloadInner;

typedef struct {

  TestStreamPayloadInner inpData_0[2];
  TestStreamPayloadInner inpData_1[2];
  TestStreamPayloadInner inpData_2[2];
  TestStreamPayloadInner outpData_0[2];
  TestStreamPayloadInner outpData_1[2];
  TestStreamPayloadInner outpData_2[2];

} TestStreamPayloadInner_1;

interface TopLevelIo () ;

  logic           push_valid ;
  logic           push_ready ;
  TestStreamPayloadInner_1 push_payload_0;
  TestStreamPayloadInner_1 push_payload_1;
  logic           pop_valid ;
  logic           pop_ready ;
  TestStreamPayloadInner_1 pop_payload_0;
  TestStreamPayloadInner_1 pop_payload_1;

  modport slv (
    output          push_valid,
    input           push_ready,
    output          push_payload_0,
    output          push_payload_1,
    input           pop_valid,
    output          pop_ready,
    input           pop_payload_0,
    input           pop_payload_1
  );

  modport mst (
    input           push_valid,
    output          push_ready,
    input           push_payload_0,
    input           push_payload_1,
    output          pop_valid,
    input           pop_ready,
    output          pop_payload_0,
    output          pop_payload_1
  );

endinterface


module TopLevel (
  TopLevelIo.mst       io
);


  TopLevelInnards dut (
    .io                                     (io)  //TopLevelIo.mst
  );

endmodule

module TopLevelInnards (
  TopLevelIo.mst       io
);


  assign io.pop_valid = io.push_valid;
  assign io.push_ready = io.pop_ready;
  assign io.pop_payload_0.inpData_0[0].myData = io.push_payload_0.inpData_0[0].myData;
  assign io.pop_payload_0.inpData_0[1].myData = io.push_payload_0.inpData_0[1].myData;
  assign io.pop_payload_0.inpData_1[0].myData = io.push_payload_0.inpData_1[0].myData;
  assign io.pop_payload_0.inpData_1[1].myData = io.push_payload_0.inpData_1[1].myData;
  assign io.pop_payload_0.inpData_2[0].myData = io.push_payload_0.inpData_2[0].myData;
  assign io.pop_payload_0.inpData_2[1].myData = io.push_payload_0.inpData_2[1].myData;
  assign io.pop_payload_0.outpData_0[0].myData = io.push_payload_0.outpData_0[0].myData;
  assign io.pop_payload_0.outpData_0[1].myData = io.push_payload_0.outpData_0[1].myData;
  assign io.pop_payload_0.outpData_1[0].myData = io.push_payload_0.outpData_1[0].myData;
  assign io.pop_payload_0.outpData_1[1].myData = io.push_payload_0.outpData_1[1].myData;
  assign io.pop_payload_0.outpData_2[0].myData = io.push_payload_0.outpData_2[0].myData;
  assign io.pop_payload_0.outpData_2[1].myData = io.push_payload_0.outpData_2[1].myData;
  assign io.pop_payload_1.inpData_0[0].myData = io.push_payload_1.inpData_0[0].myData;
  assign io.pop_payload_1.inpData_0[1].myData = io.push_payload_1.inpData_0[1].myData;
  assign io.pop_payload_1.inpData_1[0].myData = io.push_payload_1.inpData_1[0].myData;
  assign io.pop_payload_1.inpData_1[1].myData = io.push_payload_1.inpData_1[1].myData;
  assign io.pop_payload_1.inpData_2[0].myData = io.push_payload_1.inpData_2[0].myData;
  assign io.pop_payload_1.inpData_2[1].myData = io.push_payload_1.inpData_2[1].myData;
  assign io.pop_payload_1.outpData_0[0].myData = io.push_payload_1.outpData_0[0].myData;
  assign io.pop_payload_1.outpData_0[1].myData = io.push_payload_1.outpData_0[1].myData;
  assign io.pop_payload_1.outpData_1[0].myData = io.push_payload_1.outpData_1[0].myData;
  assign io.pop_payload_1.outpData_1[1].myData = io.push_payload_1.outpData_1[1].myData;
  assign io.pop_payload_1.outpData_2[0].myData = io.push_payload_1.outpData_2[0].myData;
  assign io.pop_payload_1.outpData_2[1].myData = io.push_payload_1.outpData_2[1].myData;

endmodule
