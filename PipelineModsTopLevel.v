// Generator : SpinalHDL v1.8.1    git head : 2a7592004363e5b40ec43e1f122ed8641cd8965b
// Component : PipelineModsTopLevel
// Git hash  : 12c86b140cda36662c3e8d553e58dfcc2d019a36

`timescale 1ns/1ps

module PipelineModsTopLevel (
  output              io_next_valid,
  input               io_next_ready,
  output     [0:0]    io_next_payload,
  input               io_prev_valid,
  output              io_prev_ready,
  input      [0:0]    io_prev_payload,
  input               io_misc_validBusy,
  input               io_misc_readyBusy,
  input               io_misc_clear,
  input               reset,
  input               clk
);

  wire                dut_io_next_valid;
  wire       [0:0]    dut_io_next_payload;
  wire                dut_io_prev_ready;

  PipeSkidBuf dut (
    .io_next_valid     (dut_io_next_valid  ), //o
    .io_next_ready     (io_next_ready      ), //i
    .io_next_payload   (dut_io_next_payload), //o
    .io_prev_valid     (io_prev_valid      ), //i
    .io_prev_ready     (dut_io_prev_ready  ), //o
    .io_prev_payload   (io_prev_payload    ), //i
    .io_misc_validBusy (io_misc_validBusy  ), //i
    .io_misc_readyBusy (io_misc_readyBusy  ), //i
    .io_misc_clear     (io_misc_clear      ), //i
    .reset             (reset              ), //i
    .clk               (clk                )  //i
  );
  assign io_next_valid = dut_io_next_valid;
  assign io_next_payload = dut_io_next_payload;
  assign io_prev_ready = dut_io_prev_ready;

endmodule

module PipeSkidBuf (
  output reg          io_next_valid,
  input               io_next_ready,
  output reg [0:0]    io_next_payload,
  input               io_prev_valid,
  output reg          io_prev_ready,
  input      [0:0]    io_prev_payload,
  input               io_misc_validBusy,
  input               io_misc_readyBusy,
  input               io_misc_clear,
  input               reset,
  input               clk
);

  (* keep *) reg                 _zz_io_next_valid;
  (* keep *) reg        [0:0]    _zz_io_next_payload;
  reg                 _zz_io_next_valid_1;
  reg        [0:0]    _zz_io_next_payload_1;
  wire                when_pipelineMods_l165;
  wire                when_pipelineMods_l168;
  wire                when_pipelineMods_l199;
  wire                when_pipelineMods_l230;
  reg                 formal_with_past;
  wire                when_pipelineMods_l319;
  wire                when_pipelineMods_l385;
  wire                when_pipelineMods_l392;
  reg        [0:0]    io_prev_payload_past_1;
  wire                when_pipelineMods_l397;
  wire                when_pipelineMods_l400;
  wire                when_pipelineMods_l403;

  initial begin
    formal_with_past = 1'b0;
  end

  assign when_pipelineMods_l165 = (reset || io_misc_clear);
  always @(*) begin
    if(when_pipelineMods_l165) begin
      _zz_io_next_valid_1 = 1'b0;
    end else begin
      if(when_pipelineMods_l168) begin
        _zz_io_next_valid_1 = 1'b1;
      end else begin
        if(io_next_ready) begin
          _zz_io_next_valid_1 = 1'b0;
        end else begin
          _zz_io_next_valid_1 = _zz_io_next_valid;
        end
      end
    end
  end

  assign when_pipelineMods_l168 = ((io_prev_valid && io_prev_ready) && (io_next_valid && (! io_next_ready)));
  assign when_pipelineMods_l199 = (reset || io_misc_clear);
  always @(*) begin
    if(when_pipelineMods_l199) begin
      _zz_io_next_payload_1 = 1'b0;
    end else begin
      if(io_prev_ready) begin
        _zz_io_next_payload_1 = io_prev_payload;
      end else begin
        _zz_io_next_payload_1 = _zz_io_next_payload;
      end
    end
  end

  assign when_pipelineMods_l230 = (reset || io_misc_clear);
  always @(*) begin
    if(when_pipelineMods_l230) begin
      io_prev_ready = 1'b0;
    end else begin
      io_prev_ready = (! _zz_io_next_valid);
    end
  end

  always @(*) begin
    if(when_pipelineMods_l230) begin
      io_next_valid = 1'b0;
    end else begin
      io_next_valid = (io_prev_valid || _zz_io_next_valid);
    end
  end

  always @(*) begin
    if(_zz_io_next_valid) begin
      io_next_payload = _zz_io_next_payload;
    end else begin
      io_next_payload = io_prev_payload;
    end
  end

  assign when_pipelineMods_l319 = (((formal_with_past && (! io_misc_clear)) && 1'b1) && 1'b1);
  assign when_pipelineMods_l385 = (io_next_valid && (! io_next_ready));
  assign when_pipelineMods_l392 = (((io_prev_valid && io_prev_ready) && io_next_valid) && (! io_next_ready));
  assign when_pipelineMods_l397 = (io_prev_valid && io_prev_ready);
  assign when_pipelineMods_l400 = (((! io_prev_valid) && (! _zz_io_next_valid)) && io_next_ready);
  assign when_pipelineMods_l403 = (_zz_io_next_valid && io_next_ready);
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      _zz_io_next_valid <= 1'b0;
      _zz_io_next_payload <= 1'b0;
    end else begin
      _zz_io_next_valid <= _zz_io_next_valid_1;
      _zz_io_next_payload <= _zz_io_next_payload_1;
      if(when_pipelineMods_l319) begin
        if(!reset) begin
          if(when_pipelineMods_l385) begin
            `ifndef SYNTHESIS
              `ifdef FORMAL
                assert(io_next_valid); // pipelineMods.scala:L386
              `else
                if(!io_next_valid) begin
                  $display("FAILURE "); // pipelineMods.scala:L386
                  $finish;
                end
              `endif
            `endif
          end
          if(when_pipelineMods_l392) begin
            `ifndef SYNTHESIS
              `ifdef FORMAL
                assert(_zz_io_next_valid_1); // pipelineMods.scala:L393
              `else
                if(!_zz_io_next_valid_1) begin
                  $display("FAILURE "); // pipelineMods.scala:L393
                  $finish;
                end
              `endif
            `endif
            `ifndef SYNTHESIS
              `ifdef FORMAL
                assert((_zz_io_next_payload == io_prev_payload_past_1)); // pipelineMods.scala:L394
              `else
                if(!(_zz_io_next_payload == io_prev_payload_past_1)) begin
                  $display("FAILURE "); // pipelineMods.scala:L394
                  $finish;
                end
              `endif
            `endif
          end
          if(when_pipelineMods_l397) begin
            `ifndef SYNTHESIS
              `ifdef FORMAL
                assert(io_next_valid); // pipelineMods.scala:L398
              `else
                if(!io_next_valid) begin
                  $display("FAILURE "); // pipelineMods.scala:L398
                  $finish;
                end
              `endif
            `endif
          end
          if(when_pipelineMods_l400) begin
            `ifndef SYNTHESIS
              `ifdef FORMAL
                assert((! io_next_valid)); // pipelineMods.scala:L401
              `else
                if(!(! io_next_valid)) begin
                  $display("FAILURE "); // pipelineMods.scala:L401
                  $finish;
                end
              `endif
            `endif
          end
          if(when_pipelineMods_l403) begin
            `ifndef SYNTHESIS
              `ifdef FORMAL
                assert((! _zz_io_next_valid_1)); // pipelineMods.scala:L404
              `else
                if(!(! _zz_io_next_valid_1)) begin
                  $display("FAILURE "); // pipelineMods.scala:L404
                  $finish;
                end
              `endif
            `endif
          end
        end
      end
    end
  end

  always @(posedge clk) begin
    formal_with_past <= 1'b1;
  end

  always @(posedge clk) begin
    io_prev_payload_past_1 <= io_prev_payload;
  end


endmodule
