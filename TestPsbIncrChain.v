// Generator : SpinalHDL v1.9.3    git head : 029104c77a54c53f1edda327a3bea333f7d65fd9
// Component : TestPsbIncrChain
// Git hash  : 12c86b140cda36662c3e8d553e58dfcc2d019a36

`timescale 1ns/1ps

module TestPsbIncrChain (
  input      [2:0]    io_inpData,
  output     [2:0]    io_outpData,
  input               clk,
  input               reset
);

  reg                 testPsbIncr_5_io_next_ready;
  wire                testPsbIncr_3_io_next_valid;
  wire       [2:0]    testPsbIncr_3_io_next_payload;
  wire                testPsbIncr_3_io_prev_ready;
  wire                testPsbIncr_4_io_next_valid;
  wire       [2:0]    testPsbIncr_4_io_next_payload;
  wire                testPsbIncr_4_io_prev_ready;
  wire                testPsbIncr_5_io_next_valid;
  wire       [2:0]    testPsbIncr_5_io_next_payload;
  wire                testPsbIncr_5_io_prev_ready;
  reg                 prevLastReady;

  TestPsbIncr testPsbIncr_3 (
    .io_next_valid   (testPsbIncr_3_io_next_valid       ), //o
    .io_next_ready   (testPsbIncr_4_io_prev_ready       ), //i
    .io_next_payload (testPsbIncr_3_io_next_payload[2:0]), //o
    .io_prev_valid   (1'b1                              ), //i
    .io_prev_ready   (testPsbIncr_3_io_prev_ready       ), //o
    .io_prev_payload (io_inpData[2:0]                   ), //i
    .clk             (clk                               ), //i
    .reset           (reset                             )  //i
  );
  TestPsbIncr testPsbIncr_4 (
    .io_next_valid   (testPsbIncr_4_io_next_valid       ), //o
    .io_next_ready   (testPsbIncr_5_io_prev_ready       ), //i
    .io_next_payload (testPsbIncr_4_io_next_payload[2:0]), //o
    .io_prev_valid   (testPsbIncr_3_io_next_valid       ), //i
    .io_prev_ready   (testPsbIncr_4_io_prev_ready       ), //o
    .io_prev_payload (testPsbIncr_3_io_next_payload[2:0]), //i
    .clk             (clk                               ), //i
    .reset           (reset                             )  //i
  );
  TestPsbIncr testPsbIncr_5 (
    .io_next_valid   (testPsbIncr_5_io_next_valid       ), //o
    .io_next_ready   (testPsbIncr_5_io_next_ready       ), //i
    .io_next_payload (testPsbIncr_5_io_next_payload[2:0]), //o
    .io_prev_valid   (testPsbIncr_4_io_next_valid       ), //i
    .io_prev_ready   (testPsbIncr_5_io_prev_ready       ), //o
    .io_prev_payload (testPsbIncr_4_io_next_payload[2:0]), //i
    .clk             (clk                               ), //i
    .reset           (reset                             )  //i
  );
  assign io_outpData = testPsbIncr_5_io_next_payload;
  always @(*) begin
    if(testPsbIncr_5_io_next_valid) begin
      testPsbIncr_5_io_next_ready = (! prevLastReady);
    end else begin
      testPsbIncr_5_io_next_ready = 1'b0;
    end
  end

  always @(posedge clk or posedge reset) begin
    if(reset) begin
      prevLastReady <= 1'b0;
    end else begin
      prevLastReady <= testPsbIncr_5_io_next_ready;
    end
  end


endmodule

//TestPsbIncr_2 replaced by TestPsbIncr

//TestPsbIncr_1 replaced by TestPsbIncr

module TestPsbIncr (
  output              io_next_valid,
  input               io_next_ready,
  output     [2:0]    io_next_payload,
  input               io_prev_valid,
  output              io_prev_ready,
  input      [2:0]    io_prev_payload,
  input               clk,
  input               reset
);

  wire       [2:0]    _zz_io_next_payload;
  wire                skidBuf_valid;
  wire                skidBuf_ready;
  wire       [2:0]    skidBuf_payload;
  wire                io_prev_s2mPipe_valid;
  reg                 io_prev_s2mPipe_ready;
  wire       [2:0]    io_prev_s2mPipe_payload;
  reg                 io_prev_rValidN;
  reg        [2:0]    io_prev_rData;
  wire                io_prev_s2mPipe_m2sPipe_valid;
  wire                io_prev_s2mPipe_m2sPipe_ready;
  wire       [2:0]    io_prev_s2mPipe_m2sPipe_payload;
  reg                 io_prev_s2mPipe_rValid;
  reg        [2:0]    io_prev_s2mPipe_rData;
  wire                when_Stream_l369;

  assign _zz_io_next_payload = (skidBuf_payload + 3'b001);
  assign io_prev_ready = io_prev_rValidN;
  assign io_prev_s2mPipe_valid = (io_prev_valid || (! io_prev_rValidN));
  assign io_prev_s2mPipe_payload = (io_prev_rValidN ? io_prev_payload : io_prev_rData);
  always @(*) begin
    io_prev_s2mPipe_ready = io_prev_s2mPipe_m2sPipe_ready;
    if(when_Stream_l369) begin
      io_prev_s2mPipe_ready = 1'b1;
    end
  end

  assign when_Stream_l369 = (! io_prev_s2mPipe_m2sPipe_valid);
  assign io_prev_s2mPipe_m2sPipe_valid = io_prev_s2mPipe_rValid;
  assign io_prev_s2mPipe_m2sPipe_payload = io_prev_s2mPipe_rData;
  assign skidBuf_valid = io_prev_s2mPipe_m2sPipe_valid;
  assign io_prev_s2mPipe_m2sPipe_ready = skidBuf_ready;
  assign skidBuf_payload = io_prev_s2mPipe_m2sPipe_payload;
  assign io_next_valid = skidBuf_valid;
  assign skidBuf_ready = io_next_ready;
  assign io_next_payload = _zz_io_next_payload;
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      io_prev_rValidN <= 1'b1;
      io_prev_s2mPipe_rValid <= 1'b0;
    end else begin
      if(io_prev_valid) begin
        io_prev_rValidN <= 1'b0;
      end
      if(io_prev_s2mPipe_ready) begin
        io_prev_rValidN <= 1'b1;
      end
      if(io_prev_s2mPipe_ready) begin
        io_prev_s2mPipe_rValid <= io_prev_s2mPipe_valid;
      end
    end
  end

  always @(posedge clk) begin
    if(io_prev_ready) begin
      io_prev_rData <= io_prev_payload;
    end
    if(io_prev_s2mPipe_ready) begin
      io_prev_s2mPipe_rData <= io_prev_s2mPipe_payload;
    end
  end


endmodule
