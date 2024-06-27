// Generator : SpinalHDL v1.10.2    git head : 279867b771fb50fc0aec21d8a20d8fdad0f87e3f
// Component : Clz
// Git hash  : 87c8d3496c900406422e9e81525a9c47e4bca3db

`timescale 1ns/1ps

module Clz (
  input  wire [31:0]   io_inpData,
  output reg  [5:0]    io_outpData
);

  wire       [16:0]   _zz_temp_3;
  wire       [16:0]   _zz_temp_3_1;
  wire       [15:0]   _zz_temp_3_2;
  wire       [8:0]    _zz_temp_2;
  wire       [8:0]    _zz_temp_2_1;
  wire       [7:0]    _zz_temp_2_2;
  wire       [4:0]    _zz_temp_1;
  wire       [4:0]    _zz_temp_1_1;
  wire       [3:0]    _zz_temp_1_2;
  wire       [2:0]    _zz_temp_0;
  wire       [2:0]    _zz_temp_0_1;
  wire       [1:0]    _zz_temp_0_2;
  reg        [31:0]   temp_0;
  reg        [31:0]   temp_1;
  reg        [31:0]   temp_2;
  reg        [31:0]   temp_3;
  reg        [31:0]   temp_4;
  wire                when_miscBitwiseMods_l188;

  assign _zz_temp_3 = ((temp_4[31 : 16] != 16'h0) ? _zz_temp_3_1 : {1'b1,temp_4[15 : 0]});
  assign _zz_temp_3_2 = temp_4[31 : 16];
  assign _zz_temp_3_1 = {1'd0, _zz_temp_3_2};
  assign _zz_temp_2 = ((temp_3[15 : 8] != 8'h0) ? _zz_temp_2_1 : {1'b1,temp_3[7 : 0]});
  assign _zz_temp_2_2 = temp_3[15 : 8];
  assign _zz_temp_2_1 = {1'd0, _zz_temp_2_2};
  assign _zz_temp_1 = ((temp_2[7 : 4] != 4'b0000) ? _zz_temp_1_1 : {1'b1,temp_2[3 : 0]});
  assign _zz_temp_1_2 = temp_2[7 : 4];
  assign _zz_temp_1_1 = {1'd0, _zz_temp_1_2};
  assign _zz_temp_0 = ((temp_1[3 : 2] != 2'b00) ? _zz_temp_0_1 : {1'b1,temp_1[1 : 0]});
  assign _zz_temp_0_2 = temp_1[3 : 2];
  assign _zz_temp_0_1 = {1'd0, _zz_temp_0_2};
  always @(*) begin
    temp_0 = 32'h0;
    if(!when_miscBitwiseMods_l188) begin
      temp_0 = {29'd0, _zz_temp_0};
    end
  end

  always @(*) begin
    temp_1 = 32'h0;
    if(!when_miscBitwiseMods_l188) begin
      temp_1 = {27'd0, _zz_temp_1};
    end
  end

  always @(*) begin
    temp_2 = 32'h0;
    if(!when_miscBitwiseMods_l188) begin
      temp_2 = {23'd0, _zz_temp_2};
    end
  end

  always @(*) begin
    temp_3 = 32'h0;
    if(!when_miscBitwiseMods_l188) begin
      temp_3 = {15'd0, _zz_temp_3};
    end
  end

  always @(*) begin
    temp_4 = 32'h0;
    if(!when_miscBitwiseMods_l188) begin
      temp_4 = io_inpData;
    end
  end

  always @(*) begin
    io_outpData = 6'h0;
    if(when_miscBitwiseMods_l188) begin
      io_outpData = 6'h20;
    end else begin
      io_outpData[4] = 1'b0;
      io_outpData[3] = temp_3[16];
      io_outpData[2] = temp_2[8];
      io_outpData[1] = temp_1[4];
      io_outpData[0] = temp_0[2];
    end
  end

  assign when_miscBitwiseMods_l188 = (io_inpData == 32'h0);

endmodule
