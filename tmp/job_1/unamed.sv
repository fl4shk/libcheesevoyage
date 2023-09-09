// Generator : SpinalHDL v1.9.3    git head : 029104c77a54c53f1edda327a3bea333f7d65fd9
// Component : unamed
// Git hash  : 453190cf97277f2dc6e762a573f2b5d9f43acc87

`timescale 1ns/1ps

module unamed (
  input               reset,
  input               clk
);

  wire                dut_io_push_valid;
  wire       [3:0]    dut_io_push_payload;
  wire                dut_io_pop_ready;
  wire                dut_io_push_ready;
  wire                dut_io_pop_valid;
  wire       [3:0]    dut_io_pop_payload;
  wire                dut_io_misc_empty;
  wire                dut_io_misc_full;

  Fifo dut (
    .io_push_valid   (dut_io_push_valid       ), //i
    .io_push_ready   (dut_io_push_ready       ), //o
    .io_push_payload (dut_io_push_payload[3:0]), //i
    .io_pop_valid    (dut_io_pop_valid        ), //o
    .io_pop_ready    (dut_io_pop_ready        ), //i
    .io_pop_payload  (dut_io_pop_payload[3:0] ), //o
    .io_misc_empty   (dut_io_misc_empty       ), //o
    .io_misc_full    (dut_io_misc_full        ), //o
    .reset           (reset                   ), //i
    .clk             (clk                     )  //i
  );
  initial begin
    assume(reset); // fifoModsFormal.scala:L23
    assume((dut_io_push_payload == 4'b0000)); // fifoModsFormal.scala:L27
    assume((dut_io_push_valid == 1'b0)); // fifoModsFormal.scala:L28
    assume((dut_io_pop_ready == 1'b0)); // fifoModsFormal.scala:L31
  end

  assign dut_io_push_payload = $anyseq(4);
  assign dut_io_push_valid = $anyseq(1);
  assign dut_io_pop_ready = $anyseq(1);

endmodule

module Fifo (
  input               io_push_valid,
  output              io_push_ready,
  input      [3:0]    io_push_payload,
  output              io_pop_valid,
  input               io_pop_ready,
  output     [3:0]    io_pop_payload,
  output              io_misc_empty,
  output              io_misc_full,
  input               reset,
  input               clk
);

  wire                sbPush_io_misc_clear;
  reg        [3:0]    sbPop_io_prev_payload;
  wire                sbPop_io_misc_clear;
  wire       [3:0]    _zz_loc_arr_port0;
  wire       [3:0]    _zz_loc_arr_port1;
  wire                sbPush_io_next_valid;
  wire       [3:0]    sbPush_io_next_payload;
  wire                sbPush_io_prev_ready;
  wire                sbPop_io_next_valid;
  wire       [3:0]    sbPop_io_next_payload;
  wire                sbPop_io_prev_ready;
  wire       [1:0]    _zz_loc_tailPlus1;
  wire       [1:0]    _zz_loc_tailPlus1_1;
  wire       [0:0]    _zz_loc_tailPlus1_2;
  wire       [1:0]    _zz_loc_headPlus1;
  wire       [1:0]    _zz_loc_headPlus1_1;
  wire       [0:0]    _zz_loc_headPlus1_2;
  wire       [1:0]    _zz_locFormal_testHead;
  wire       [1:0]    _zz_loc_nextFull;
  wire       [3:0]    _zz_loc_arr_port;
  reg                 _zz_1;
  wire                wrEn;
  reg                 rdValid;
  reg        [3:0]    rdDataPrev;
  wire                rdEn;
  reg        [1:0]    loc_tail;
  reg        [1:0]    loc_head;
  wire       [2:0]    loc_tailPlus1;
  wire       [2:0]    loc_headPlus1;
  reg        [1:0]    loc_incrTail;
  reg        [1:0]    loc_incrHead;
  wire                loc_nextEmpty;
  reg                 loc_nextFull;
  reg                 loc_rEmpty;
  reg                 loc_rFull;
  reg        [1:0]    loc_nextTail;
  reg        [1:0]    loc_nextHead;
  reg        [3:0]    locFormal_lastTailVal;
  wire       [1:0]    locFormal_testHead;
  reg        [31:0]   locFormal_wdCnt;
  wire                when_fifoMods_l217;
  wire                when_fifoMods_l223;
  wire                when_fifoMods_l237;
  wire                when_fifoMods_l245;
  wire                when_fifoMods_l308;
  wire       [1:0]    _zz_io_prev_payload;
  wire                when_fifoMods_l318;
  reg                 formal_with_past_after_reset;
  reg                 loc_nextEmpty_past_1;
  reg                 loc_nextFull_past_1;
  reg        [1:0]    loc_nextTail_past_1;
  reg        [1:0]    loc_nextHead_past_1;
  reg                 rdEn_past_1;
  reg                 io_misc_empty_past_1;
  reg        [1:0]    loc_tail_regNext;
  reg                 wrEn_past_1;
  reg                 io_misc_full_past_1;
  reg        [1:0]    loc_head_regNext;
  wire       [1:0]    switch_fifoMods_l366;
  (* ram_style = "ultra" , keep *) reg [3:0] loc_arr [0:3];

  assign _zz_loc_tailPlus1 = (loc_tail + _zz_loc_tailPlus1_1);
  assign _zz_loc_tailPlus1_2 = 1'b1;
  assign _zz_loc_tailPlus1_1 = {1'd0, _zz_loc_tailPlus1_2};
  assign _zz_loc_headPlus1 = (loc_head + _zz_loc_headPlus1_1);
  assign _zz_loc_headPlus1_2 = 1'b1;
  assign _zz_loc_headPlus1_1 = {1'd0, _zz_loc_headPlus1_2};
  assign _zz_locFormal_testHead = (loc_head + 2'b01);
  assign _zz_loc_nextFull = (loc_incrHead + 2'b01);
  assign _zz_loc_arr_port = io_push_payload;
  assign _zz_loc_arr_port0 = loc_arr[loc_tail];
  assign _zz_loc_arr_port1 = loc_arr[_zz_io_prev_payload];
  always @(posedge clk) begin
    if(_zz_1) begin
      loc_arr[loc_head] <= _zz_loc_arr_port;
    end
  end

  PipeSkidBuf sbPush (
    .io_next_valid   (sbPush_io_next_valid       ), //o
    .io_next_ready   (1'b1                       ), //i
    .io_next_payload (sbPush_io_next_payload[3:0]), //o
    .io_prev_valid   (io_push_valid              ), //i
    .io_prev_ready   (sbPush_io_prev_ready       ), //o
    .io_prev_payload (io_push_payload[3:0]       ), //i
    .io_misc_clear   (sbPush_io_misc_clear       ), //i
    .reset           (reset                      ), //i
    .clk             (clk                        )  //i
  );
  PipeSkidBuf sbPop (
    .io_next_valid   (sbPop_io_next_valid       ), //o
    .io_next_ready   (io_pop_ready              ), //i
    .io_next_payload (sbPop_io_next_payload[3:0]), //o
    .io_prev_valid   (rdValid                   ), //i
    .io_prev_ready   (sbPop_io_prev_ready       ), //o
    .io_prev_payload (sbPop_io_prev_payload[3:0]), //i
    .io_misc_clear   (sbPop_io_misc_clear       ), //i
    .reset           (reset                     ), //i
    .clk             (clk                       )  //i
  );
  always @(*) begin
    _zz_1 = 1'b0;
    if(!reset) begin
      if(when_fifoMods_l318) begin
        _zz_1 = 1'b1;
      end
    end
  end

  assign io_push_ready = sbPush_io_prev_ready;
  assign wrEn = (io_push_valid && sbPush_io_prev_ready);
  assign io_pop_valid = sbPop_io_next_valid;
  assign io_pop_payload = sbPop_io_next_payload;
  assign rdEn = (sbPop_io_next_valid && io_pop_ready);
  assign loc_tailPlus1 = {1'd0, _zz_loc_tailPlus1};
  assign loc_headPlus1 = {1'd0, _zz_loc_headPlus1};
  assign locFormal_testHead = (_zz_locFormal_testHead % 3'b100);
  assign when_fifoMods_l217 = (loc_tailPlus1 < 3'b100);
  always @(*) begin
    if(when_fifoMods_l217) begin
      loc_incrTail = (loc_tail + 2'b01);
    end else begin
      loc_incrTail = 2'b00;
    end
  end

  assign when_fifoMods_l223 = (loc_headPlus1 < 3'b100);
  always @(*) begin
    if(when_fifoMods_l223) begin
      loc_incrHead = (loc_head + 2'b01);
    end else begin
      loc_incrHead = 2'b00;
    end
  end

  assign loc_nextEmpty = (loc_nextHead == loc_nextTail);
  assign when_fifoMods_l237 = (rdEn && (! io_misc_empty));
  always @(*) begin
    if(when_fifoMods_l237) begin
      loc_nextTail = loc_incrTail;
    end else begin
      loc_nextTail = loc_tail;
    end
  end

  assign when_fifoMods_l245 = (wrEn && (! io_misc_full));
  always @(*) begin
    if(when_fifoMods_l245) begin
      loc_nextHead = loc_incrHead;
    end else begin
      loc_nextHead = loc_head;
    end
  end

  always @(*) begin
    if(when_fifoMods_l245) begin
      loc_nextFull = (_zz_loc_nextFull == loc_nextTail);
    end else begin
      loc_nextFull = (loc_incrHead == loc_nextTail);
    end
  end

  assign io_misc_empty = loc_rEmpty;
  assign io_misc_full = loc_rFull;
  always @(*) begin
    if(reset) begin
      sbPop_io_prev_payload = 4'b0000;
    end else begin
      if(when_fifoMods_l308) begin
        sbPop_io_prev_payload = _zz_loc_arr_port1;
      end else begin
        sbPop_io_prev_payload = rdDataPrev;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      rdValid = 1'b0;
    end else begin
      if(when_fifoMods_l308) begin
        rdValid = 1'b1;
      end else begin
        rdValid = 1'b0;
      end
    end
  end

  assign when_fifoMods_l308 = (rdEn && (! io_misc_empty));
  assign _zz_io_prev_payload = loc_tail;
  assign when_fifoMods_l318 = (wrEn && (! io_misc_full));
  assign switch_fifoMods_l366 = {io_misc_full,io_misc_empty};
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      rdDataPrev <= 4'b0000;
      loc_tail <= 2'b00;
      loc_head <= 2'b00;
      loc_rEmpty <= 1'b1;
      loc_rFull <= 1'b0;
      locFormal_lastTailVal <= 4'b0000;
      locFormal_wdCnt <= 32'h00000000;
    end else begin
      locFormal_lastTailVal <= _zz_loc_arr_port0;
      locFormal_wdCnt <= (locFormal_wdCnt - 32'h00000010);
      rdDataPrev <= sbPop_io_prev_payload;
      if(reset) begin
        loc_tail <= 2'b00;
        loc_head <= 2'b00;
      end else begin
        loc_rEmpty <= loc_nextEmpty;
        loc_rFull <= loc_nextFull;
        loc_tail <= loc_nextTail;
        loc_head <= loc_nextHead;
        if(formal_with_past_after_reset) begin
          assert((io_misc_empty == loc_nextEmpty_past_1)); // fifoMods.scala:L328
          assert((io_misc_full == loc_nextFull_past_1)); // fifoMods.scala:L329
          assert((loc_tail == loc_nextTail_past_1)); // fifoMods.scala:L330
          assert((loc_head == loc_nextHead_past_1)); // fifoMods.scala:L331
          if(rdEn_past_1) begin
            if(io_misc_empty_past_1) begin
              assert((loc_tail_regNext == loc_tail)); // fifoMods.scala:L337
            end else begin
              assert((rdDataPrev == locFormal_lastTailVal)); // fifoMods.scala:L345
            end
          end
          if(wrEn_past_1) begin
            if(io_misc_full_past_1) begin
              assert((loc_head_regNext == loc_head)); // fifoMods.scala:L352
            end
          end
          case(switch_fifoMods_l366)
            2'b00 : begin
              assume((loc_head != loc_tail)); // fifoMods.scala:L370
              assume((locFormal_testHead != loc_tail)); // fifoMods.scala:L371
            end
            2'b01 : begin
              assert((loc_head == loc_tail)); // fifoMods.scala:L377
            end
            2'b10 : begin
              assert((locFormal_testHead == loc_tail)); // fifoMods.scala:L383
            end
            default : begin
            end
          endcase
          assert((! (io_misc_empty && io_misc_full))); // fifoMods.scala:L388
        end
      end
    end
  end

  always @(posedge clk or posedge reset) begin
    if(reset) begin
      formal_with_past_after_reset <= 1'b0;
    end else begin
      formal_with_past_after_reset <= (! reset);
    end
  end

  always @(posedge clk) begin
    loc_nextEmpty_past_1 <= loc_nextEmpty;
    loc_nextFull_past_1 <= loc_nextFull;
    loc_nextTail_past_1 <= loc_nextTail;
    loc_nextHead_past_1 <= loc_nextHead;
    rdEn_past_1 <= rdEn;
    wrEn_past_1 <= wrEn;
  end

  always @(posedge clk) begin
    io_misc_empty_past_1 <= io_misc_empty;
  end

  always @(posedge clk) begin
    loc_tail_regNext <= loc_tail;
  end

  always @(posedge clk) begin
    io_misc_full_past_1 <= io_misc_full;
  end

  always @(posedge clk) begin
    loc_head_regNext <= loc_head;
  end


endmodule

//PipeSkidBuf_1 replaced by PipeSkidBuf

module PipeSkidBuf (
  (* keep *) output              io_next_valid,
  (* keep *) input               io_next_ready,
  (* keep *) output reg [3:0]    io_next_payload,
  (* keep *) input               io_prev_valid,
  (* keep *) output              io_prev_ready,
  (* keep *) input      [3:0]    io_prev_payload,
  (* keep *) input               io_misc_clear,
  input               reset,
  input               clk
);

  (* keep *) wire                _zz_io_next_valid;
  (* keep *) wire                _zz_io_prev_s2mPipe_m2sPipe_ready;
  (* keep *) wire       [3:0]    _zz_io_next_payload;
  wire                io_prev_s2mPipe_valid;
  reg                 io_prev_s2mPipe_ready;
  wire       [3:0]    io_prev_s2mPipe_payload;
  reg                 io_prev_rValidN;
  reg        [3:0]    io_prev_rData;
  wire                io_prev_s2mPipe_m2sPipe_valid;
  wire                io_prev_s2mPipe_m2sPipe_ready;
  wire       [3:0]    io_prev_s2mPipe_m2sPipe_payload;
  reg                 io_prev_s2mPipe_rValid;
  reg        [3:0]    io_prev_s2mPipe_rData;
  wire                when_Stream_l369;
  wire                io_prev_fire;
  reg                 _zz_1;
  reg        [3:0]    _zz_2;
  wire                io_next_isStall;
  reg                 io_next_isStall_past_1;
  reg        [3:0]    io_next_payload_regNext;

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
  assign _zz_io_next_valid = io_prev_s2mPipe_m2sPipe_valid;
  assign io_prev_s2mPipe_m2sPipe_ready = _zz_io_prev_s2mPipe_m2sPipe_ready;
  assign _zz_io_next_payload = io_prev_s2mPipe_m2sPipe_payload;
  assign io_next_valid = _zz_io_next_valid;
  assign _zz_io_prev_s2mPipe_m2sPipe_ready = io_next_ready;
  always @(*) begin
    if(reset) begin
      io_next_payload = 4'b0000;
    end else begin
      if(io_prev_fire) begin
        io_next_payload = _zz_io_next_payload;
      end else begin
        io_next_payload = 4'b0000;
      end
    end
  end

  assign io_prev_fire = (io_prev_valid && io_prev_ready);
  assign io_next_isStall = (io_next_valid && (! io_next_ready));
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      io_prev_rValidN <= 1'b1;
      io_prev_s2mPipe_rValid <= 1'b0;
      _zz_1 <= 1'b0;
      io_next_isStall_past_1 <= 1'b0;
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
      _zz_1 <= (_zz_io_next_valid && (! _zz_io_prev_s2mPipe_m2sPipe_ready));
      if(_zz_1) begin
        assert(_zz_io_next_valid); // pipelineMods.scala:L226
        assert((_zz_2 == _zz_io_next_payload)); // pipelineMods.scala:L226
      end
      io_next_isStall_past_1 <= io_next_isStall;
      if(io_next_isStall_past_1) begin
        assume(io_next_valid); // pipelineMods.scala:L228
        assume((io_next_payload_regNext == io_next_payload)); // pipelineMods.scala:L228
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

  always @(posedge clk) begin
    _zz_2 <= _zz_io_next_payload;
  end

  always @(posedge clk) begin
    io_next_payload_regNext <= io_next_payload;
  end


endmodule
