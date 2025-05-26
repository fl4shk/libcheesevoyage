// Generator : SpinalHDL v1.10.2a    git head : a348a60b7e8b6a455c72e1536ec3d74a2ea16935
// Component : TopLevel
// Git hash  : e944dd9459c9a01bc219bd2ac237965291fa000e

`timescale 1ns/1ps

module TopLevel (
  input  wire          io_push_valid,
  output wire          io_push_ready,
  input  wire [7:0]    io_push_payload_0_inpData_0_0_myData,
  input  wire [7:0]    io_push_payload_0_inpData_0_1_myData,
  input  wire [7:0]    io_push_payload_0_inpData_1_0_myData,
  input  wire [7:0]    io_push_payload_0_inpData_1_1_myData,
  input  wire [7:0]    io_push_payload_0_inpData_2_0_myData,
  input  wire [7:0]    io_push_payload_0_inpData_2_1_myData,
  input  wire [7:0]    io_push_payload_0_outpData_0_0_myData,
  input  wire [7:0]    io_push_payload_0_outpData_0_1_myData,
  input  wire [7:0]    io_push_payload_0_outpData_1_0_myData,
  input  wire [7:0]    io_push_payload_0_outpData_1_1_myData,
  input  wire [7:0]    io_push_payload_0_outpData_2_0_myData,
  input  wire [7:0]    io_push_payload_0_outpData_2_1_myData,
  input  wire [7:0]    io_push_payload_1_inpData_0_0_myData,
  input  wire [7:0]    io_push_payload_1_inpData_0_1_myData,
  input  wire [7:0]    io_push_payload_1_inpData_1_0_myData,
  input  wire [7:0]    io_push_payload_1_inpData_1_1_myData,
  input  wire [7:0]    io_push_payload_1_inpData_2_0_myData,
  input  wire [7:0]    io_push_payload_1_inpData_2_1_myData,
  input  wire [7:0]    io_push_payload_1_outpData_0_0_myData,
  input  wire [7:0]    io_push_payload_1_outpData_0_1_myData,
  input  wire [7:0]    io_push_payload_1_outpData_1_0_myData,
  input  wire [7:0]    io_push_payload_1_outpData_1_1_myData,
  input  wire [7:0]    io_push_payload_1_outpData_2_0_myData,
  input  wire [7:0]    io_push_payload_1_outpData_2_1_myData,
  output wire          io_pop_valid,
  input  wire          io_pop_ready,
  output wire [7:0]    io_pop_payload_0_inpData_0_0_myData,
  output wire [7:0]    io_pop_payload_0_inpData_0_1_myData,
  output wire [7:0]    io_pop_payload_0_inpData_1_0_myData,
  output wire [7:0]    io_pop_payload_0_inpData_1_1_myData,
  output wire [7:0]    io_pop_payload_0_inpData_2_0_myData,
  output wire [7:0]    io_pop_payload_0_inpData_2_1_myData,
  output wire [7:0]    io_pop_payload_0_outpData_0_0_myData,
  output wire [7:0]    io_pop_payload_0_outpData_0_1_myData,
  output wire [7:0]    io_pop_payload_0_outpData_1_0_myData,
  output wire [7:0]    io_pop_payload_0_outpData_1_1_myData,
  output wire [7:0]    io_pop_payload_0_outpData_2_0_myData,
  output wire [7:0]    io_pop_payload_0_outpData_2_1_myData,
  output wire [7:0]    io_pop_payload_1_inpData_0_0_myData,
  output wire [7:0]    io_pop_payload_1_inpData_0_1_myData,
  output wire [7:0]    io_pop_payload_1_inpData_1_0_myData,
  output wire [7:0]    io_pop_payload_1_inpData_1_1_myData,
  output wire [7:0]    io_pop_payload_1_inpData_2_0_myData,
  output wire [7:0]    io_pop_payload_1_inpData_2_1_myData,
  output wire [7:0]    io_pop_payload_1_outpData_0_0_myData,
  output wire [7:0]    io_pop_payload_1_outpData_0_1_myData,
  output wire [7:0]    io_pop_payload_1_outpData_1_0_myData,
  output wire [7:0]    io_pop_payload_1_outpData_1_1_myData,
  output wire [7:0]    io_pop_payload_1_outpData_2_0_myData,
  output wire [7:0]    io_pop_payload_1_outpData_2_1_myData
);

  wire                dut_io_push_ready;
  wire                dut_io_pop_valid;
  wire       [7:0]    dut_io_pop_payload_0_inpData_0_0_myData;
  wire       [7:0]    dut_io_pop_payload_0_inpData_0_1_myData;
  wire       [7:0]    dut_io_pop_payload_0_inpData_1_0_myData;
  wire       [7:0]    dut_io_pop_payload_0_inpData_1_1_myData;
  wire       [7:0]    dut_io_pop_payload_0_inpData_2_0_myData;
  wire       [7:0]    dut_io_pop_payload_0_inpData_2_1_myData;
  wire       [7:0]    dut_io_pop_payload_0_outpData_0_0_myData;
  wire       [7:0]    dut_io_pop_payload_0_outpData_0_1_myData;
  wire       [7:0]    dut_io_pop_payload_0_outpData_1_0_myData;
  wire       [7:0]    dut_io_pop_payload_0_outpData_1_1_myData;
  wire       [7:0]    dut_io_pop_payload_0_outpData_2_0_myData;
  wire       [7:0]    dut_io_pop_payload_0_outpData_2_1_myData;
  wire       [7:0]    dut_io_pop_payload_1_inpData_0_0_myData;
  wire       [7:0]    dut_io_pop_payload_1_inpData_0_1_myData;
  wire       [7:0]    dut_io_pop_payload_1_inpData_1_0_myData;
  wire       [7:0]    dut_io_pop_payload_1_inpData_1_1_myData;
  wire       [7:0]    dut_io_pop_payload_1_inpData_2_0_myData;
  wire       [7:0]    dut_io_pop_payload_1_inpData_2_1_myData;
  wire       [7:0]    dut_io_pop_payload_1_outpData_0_0_myData;
  wire       [7:0]    dut_io_pop_payload_1_outpData_0_1_myData;
  wire       [7:0]    dut_io_pop_payload_1_outpData_1_0_myData;
  wire       [7:0]    dut_io_pop_payload_1_outpData_1_1_myData;
  wire       [7:0]    dut_io_pop_payload_1_outpData_2_0_myData;
  wire       [7:0]    dut_io_pop_payload_1_outpData_2_1_myData;

  TopLevelInnards dut (
    .io_push_valid                         (io_push_valid                                ), //i
    .io_push_ready                         (dut_io_push_ready                            ), //o
    .io_push_payload_0_inpData_0_0_myData  (io_push_payload_0_inpData_0_0_myData[7:0]    ), //i
    .io_push_payload_0_inpData_0_1_myData  (io_push_payload_0_inpData_0_1_myData[7:0]    ), //i
    .io_push_payload_0_inpData_1_0_myData  (io_push_payload_0_inpData_1_0_myData[7:0]    ), //i
    .io_push_payload_0_inpData_1_1_myData  (io_push_payload_0_inpData_1_1_myData[7:0]    ), //i
    .io_push_payload_0_inpData_2_0_myData  (io_push_payload_0_inpData_2_0_myData[7:0]    ), //i
    .io_push_payload_0_inpData_2_1_myData  (io_push_payload_0_inpData_2_1_myData[7:0]    ), //i
    .io_push_payload_0_outpData_0_0_myData (io_push_payload_0_outpData_0_0_myData[7:0]   ), //i
    .io_push_payload_0_outpData_0_1_myData (io_push_payload_0_outpData_0_1_myData[7:0]   ), //i
    .io_push_payload_0_outpData_1_0_myData (io_push_payload_0_outpData_1_0_myData[7:0]   ), //i
    .io_push_payload_0_outpData_1_1_myData (io_push_payload_0_outpData_1_1_myData[7:0]   ), //i
    .io_push_payload_0_outpData_2_0_myData (io_push_payload_0_outpData_2_0_myData[7:0]   ), //i
    .io_push_payload_0_outpData_2_1_myData (io_push_payload_0_outpData_2_1_myData[7:0]   ), //i
    .io_push_payload_1_inpData_0_0_myData  (io_push_payload_1_inpData_0_0_myData[7:0]    ), //i
    .io_push_payload_1_inpData_0_1_myData  (io_push_payload_1_inpData_0_1_myData[7:0]    ), //i
    .io_push_payload_1_inpData_1_0_myData  (io_push_payload_1_inpData_1_0_myData[7:0]    ), //i
    .io_push_payload_1_inpData_1_1_myData  (io_push_payload_1_inpData_1_1_myData[7:0]    ), //i
    .io_push_payload_1_inpData_2_0_myData  (io_push_payload_1_inpData_2_0_myData[7:0]    ), //i
    .io_push_payload_1_inpData_2_1_myData  (io_push_payload_1_inpData_2_1_myData[7:0]    ), //i
    .io_push_payload_1_outpData_0_0_myData (io_push_payload_1_outpData_0_0_myData[7:0]   ), //i
    .io_push_payload_1_outpData_0_1_myData (io_push_payload_1_outpData_0_1_myData[7:0]   ), //i
    .io_push_payload_1_outpData_1_0_myData (io_push_payload_1_outpData_1_0_myData[7:0]   ), //i
    .io_push_payload_1_outpData_1_1_myData (io_push_payload_1_outpData_1_1_myData[7:0]   ), //i
    .io_push_payload_1_outpData_2_0_myData (io_push_payload_1_outpData_2_0_myData[7:0]   ), //i
    .io_push_payload_1_outpData_2_1_myData (io_push_payload_1_outpData_2_1_myData[7:0]   ), //i
    .io_pop_valid                          (dut_io_pop_valid                             ), //o
    .io_pop_ready                          (io_pop_ready                                 ), //i
    .io_pop_payload_0_inpData_0_0_myData   (dut_io_pop_payload_0_inpData_0_0_myData[7:0] ), //o
    .io_pop_payload_0_inpData_0_1_myData   (dut_io_pop_payload_0_inpData_0_1_myData[7:0] ), //o
    .io_pop_payload_0_inpData_1_0_myData   (dut_io_pop_payload_0_inpData_1_0_myData[7:0] ), //o
    .io_pop_payload_0_inpData_1_1_myData   (dut_io_pop_payload_0_inpData_1_1_myData[7:0] ), //o
    .io_pop_payload_0_inpData_2_0_myData   (dut_io_pop_payload_0_inpData_2_0_myData[7:0] ), //o
    .io_pop_payload_0_inpData_2_1_myData   (dut_io_pop_payload_0_inpData_2_1_myData[7:0] ), //o
    .io_pop_payload_0_outpData_0_0_myData  (dut_io_pop_payload_0_outpData_0_0_myData[7:0]), //o
    .io_pop_payload_0_outpData_0_1_myData  (dut_io_pop_payload_0_outpData_0_1_myData[7:0]), //o
    .io_pop_payload_0_outpData_1_0_myData  (dut_io_pop_payload_0_outpData_1_0_myData[7:0]), //o
    .io_pop_payload_0_outpData_1_1_myData  (dut_io_pop_payload_0_outpData_1_1_myData[7:0]), //o
    .io_pop_payload_0_outpData_2_0_myData  (dut_io_pop_payload_0_outpData_2_0_myData[7:0]), //o
    .io_pop_payload_0_outpData_2_1_myData  (dut_io_pop_payload_0_outpData_2_1_myData[7:0]), //o
    .io_pop_payload_1_inpData_0_0_myData   (dut_io_pop_payload_1_inpData_0_0_myData[7:0] ), //o
    .io_pop_payload_1_inpData_0_1_myData   (dut_io_pop_payload_1_inpData_0_1_myData[7:0] ), //o
    .io_pop_payload_1_inpData_1_0_myData   (dut_io_pop_payload_1_inpData_1_0_myData[7:0] ), //o
    .io_pop_payload_1_inpData_1_1_myData   (dut_io_pop_payload_1_inpData_1_1_myData[7:0] ), //o
    .io_pop_payload_1_inpData_2_0_myData   (dut_io_pop_payload_1_inpData_2_0_myData[7:0] ), //o
    .io_pop_payload_1_inpData_2_1_myData   (dut_io_pop_payload_1_inpData_2_1_myData[7:0] ), //o
    .io_pop_payload_1_outpData_0_0_myData  (dut_io_pop_payload_1_outpData_0_0_myData[7:0]), //o
    .io_pop_payload_1_outpData_0_1_myData  (dut_io_pop_payload_1_outpData_0_1_myData[7:0]), //o
    .io_pop_payload_1_outpData_1_0_myData  (dut_io_pop_payload_1_outpData_1_0_myData[7:0]), //o
    .io_pop_payload_1_outpData_1_1_myData  (dut_io_pop_payload_1_outpData_1_1_myData[7:0]), //o
    .io_pop_payload_1_outpData_2_0_myData  (dut_io_pop_payload_1_outpData_2_0_myData[7:0]), //o
    .io_pop_payload_1_outpData_2_1_myData  (dut_io_pop_payload_1_outpData_2_1_myData[7:0])  //o
  );
  assign io_push_ready = dut_io_push_ready;
  assign io_pop_valid = dut_io_pop_valid;
  assign io_pop_payload_0_inpData_0_0_myData = dut_io_pop_payload_0_inpData_0_0_myData;
  assign io_pop_payload_0_inpData_0_1_myData = dut_io_pop_payload_0_inpData_0_1_myData;
  assign io_pop_payload_0_inpData_1_0_myData = dut_io_pop_payload_0_inpData_1_0_myData;
  assign io_pop_payload_0_inpData_1_1_myData = dut_io_pop_payload_0_inpData_1_1_myData;
  assign io_pop_payload_0_inpData_2_0_myData = dut_io_pop_payload_0_inpData_2_0_myData;
  assign io_pop_payload_0_inpData_2_1_myData = dut_io_pop_payload_0_inpData_2_1_myData;
  assign io_pop_payload_0_outpData_0_0_myData = dut_io_pop_payload_0_outpData_0_0_myData;
  assign io_pop_payload_0_outpData_0_1_myData = dut_io_pop_payload_0_outpData_0_1_myData;
  assign io_pop_payload_0_outpData_1_0_myData = dut_io_pop_payload_0_outpData_1_0_myData;
  assign io_pop_payload_0_outpData_1_1_myData = dut_io_pop_payload_0_outpData_1_1_myData;
  assign io_pop_payload_0_outpData_2_0_myData = dut_io_pop_payload_0_outpData_2_0_myData;
  assign io_pop_payload_0_outpData_2_1_myData = dut_io_pop_payload_0_outpData_2_1_myData;
  assign io_pop_payload_1_inpData_0_0_myData = dut_io_pop_payload_1_inpData_0_0_myData;
  assign io_pop_payload_1_inpData_0_1_myData = dut_io_pop_payload_1_inpData_0_1_myData;
  assign io_pop_payload_1_inpData_1_0_myData = dut_io_pop_payload_1_inpData_1_0_myData;
  assign io_pop_payload_1_inpData_1_1_myData = dut_io_pop_payload_1_inpData_1_1_myData;
  assign io_pop_payload_1_inpData_2_0_myData = dut_io_pop_payload_1_inpData_2_0_myData;
  assign io_pop_payload_1_inpData_2_1_myData = dut_io_pop_payload_1_inpData_2_1_myData;
  assign io_pop_payload_1_outpData_0_0_myData = dut_io_pop_payload_1_outpData_0_0_myData;
  assign io_pop_payload_1_outpData_0_1_myData = dut_io_pop_payload_1_outpData_0_1_myData;
  assign io_pop_payload_1_outpData_1_0_myData = dut_io_pop_payload_1_outpData_1_0_myData;
  assign io_pop_payload_1_outpData_1_1_myData = dut_io_pop_payload_1_outpData_1_1_myData;
  assign io_pop_payload_1_outpData_2_0_myData = dut_io_pop_payload_1_outpData_2_0_myData;
  assign io_pop_payload_1_outpData_2_1_myData = dut_io_pop_payload_1_outpData_2_1_myData;

endmodule

module TopLevelInnards (
  input  wire          io_push_valid,
  output wire          io_push_ready,
  input  wire [7:0]    io_push_payload_0_inpData_0_0_myData,
  input  wire [7:0]    io_push_payload_0_inpData_0_1_myData,
  input  wire [7:0]    io_push_payload_0_inpData_1_0_myData,
  input  wire [7:0]    io_push_payload_0_inpData_1_1_myData,
  input  wire [7:0]    io_push_payload_0_inpData_2_0_myData,
  input  wire [7:0]    io_push_payload_0_inpData_2_1_myData,
  input  wire [7:0]    io_push_payload_0_outpData_0_0_myData,
  input  wire [7:0]    io_push_payload_0_outpData_0_1_myData,
  input  wire [7:0]    io_push_payload_0_outpData_1_0_myData,
  input  wire [7:0]    io_push_payload_0_outpData_1_1_myData,
  input  wire [7:0]    io_push_payload_0_outpData_2_0_myData,
  input  wire [7:0]    io_push_payload_0_outpData_2_1_myData,
  input  wire [7:0]    io_push_payload_1_inpData_0_0_myData,
  input  wire [7:0]    io_push_payload_1_inpData_0_1_myData,
  input  wire [7:0]    io_push_payload_1_inpData_1_0_myData,
  input  wire [7:0]    io_push_payload_1_inpData_1_1_myData,
  input  wire [7:0]    io_push_payload_1_inpData_2_0_myData,
  input  wire [7:0]    io_push_payload_1_inpData_2_1_myData,
  input  wire [7:0]    io_push_payload_1_outpData_0_0_myData,
  input  wire [7:0]    io_push_payload_1_outpData_0_1_myData,
  input  wire [7:0]    io_push_payload_1_outpData_1_0_myData,
  input  wire [7:0]    io_push_payload_1_outpData_1_1_myData,
  input  wire [7:0]    io_push_payload_1_outpData_2_0_myData,
  input  wire [7:0]    io_push_payload_1_outpData_2_1_myData,
  output wire          io_pop_valid,
  input  wire          io_pop_ready,
  output wire [7:0]    io_pop_payload_0_inpData_0_0_myData,
  output wire [7:0]    io_pop_payload_0_inpData_0_1_myData,
  output wire [7:0]    io_pop_payload_0_inpData_1_0_myData,
  output wire [7:0]    io_pop_payload_0_inpData_1_1_myData,
  output wire [7:0]    io_pop_payload_0_inpData_2_0_myData,
  output wire [7:0]    io_pop_payload_0_inpData_2_1_myData,
  output wire [7:0]    io_pop_payload_0_outpData_0_0_myData,
  output wire [7:0]    io_pop_payload_0_outpData_0_1_myData,
  output wire [7:0]    io_pop_payload_0_outpData_1_0_myData,
  output wire [7:0]    io_pop_payload_0_outpData_1_1_myData,
  output wire [7:0]    io_pop_payload_0_outpData_2_0_myData,
  output wire [7:0]    io_pop_payload_0_outpData_2_1_myData,
  output wire [7:0]    io_pop_payload_1_inpData_0_0_myData,
  output wire [7:0]    io_pop_payload_1_inpData_0_1_myData,
  output wire [7:0]    io_pop_payload_1_inpData_1_0_myData,
  output wire [7:0]    io_pop_payload_1_inpData_1_1_myData,
  output wire [7:0]    io_pop_payload_1_inpData_2_0_myData,
  output wire [7:0]    io_pop_payload_1_inpData_2_1_myData,
  output wire [7:0]    io_pop_payload_1_outpData_0_0_myData,
  output wire [7:0]    io_pop_payload_1_outpData_0_1_myData,
  output wire [7:0]    io_pop_payload_1_outpData_1_0_myData,
  output wire [7:0]    io_pop_payload_1_outpData_1_1_myData,
  output wire [7:0]    io_pop_payload_1_outpData_2_0_myData,
  output wire [7:0]    io_pop_payload_1_outpData_2_1_myData
);


  assign io_pop_valid = io_push_valid;
  assign io_push_ready = io_pop_ready;
  assign io_pop_payload_0_inpData_0_0_myData = io_push_payload_0_inpData_0_0_myData;
  assign io_pop_payload_0_inpData_0_1_myData = io_push_payload_0_inpData_0_1_myData;
  assign io_pop_payload_0_inpData_1_0_myData = io_push_payload_0_inpData_1_0_myData;
  assign io_pop_payload_0_inpData_1_1_myData = io_push_payload_0_inpData_1_1_myData;
  assign io_pop_payload_0_inpData_2_0_myData = io_push_payload_0_inpData_2_0_myData;
  assign io_pop_payload_0_inpData_2_1_myData = io_push_payload_0_inpData_2_1_myData;
  assign io_pop_payload_0_outpData_0_0_myData = io_push_payload_0_outpData_0_0_myData;
  assign io_pop_payload_0_outpData_0_1_myData = io_push_payload_0_outpData_0_1_myData;
  assign io_pop_payload_0_outpData_1_0_myData = io_push_payload_0_outpData_1_0_myData;
  assign io_pop_payload_0_outpData_1_1_myData = io_push_payload_0_outpData_1_1_myData;
  assign io_pop_payload_0_outpData_2_0_myData = io_push_payload_0_outpData_2_0_myData;
  assign io_pop_payload_0_outpData_2_1_myData = io_push_payload_0_outpData_2_1_myData;
  assign io_pop_payload_1_inpData_0_0_myData = io_push_payload_1_inpData_0_0_myData;
  assign io_pop_payload_1_inpData_0_1_myData = io_push_payload_1_inpData_0_1_myData;
  assign io_pop_payload_1_inpData_1_0_myData = io_push_payload_1_inpData_1_0_myData;
  assign io_pop_payload_1_inpData_1_1_myData = io_push_payload_1_inpData_1_1_myData;
  assign io_pop_payload_1_inpData_2_0_myData = io_push_payload_1_inpData_2_0_myData;
  assign io_pop_payload_1_inpData_2_1_myData = io_push_payload_1_inpData_2_1_myData;
  assign io_pop_payload_1_outpData_0_0_myData = io_push_payload_1_outpData_0_0_myData;
  assign io_pop_payload_1_outpData_0_1_myData = io_push_payload_1_outpData_0_1_myData;
  assign io_pop_payload_1_outpData_1_0_myData = io_push_payload_1_outpData_1_0_myData;
  assign io_pop_payload_1_outpData_1_1_myData = io_push_payload_1_outpData_1_1_myData;
  assign io_pop_payload_1_outpData_2_0_myData = io_push_payload_1_outpData_2_0_myData;
  assign io_pop_payload_1_outpData_2_1_myData = io_push_payload_1_outpData_2_1_myData;

endmodule
