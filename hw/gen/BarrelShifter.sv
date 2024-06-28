// Generator : SpinalHDL v1.10.2    git head : 279867b771fb50fc0aec21d8a20d8fdad0f87e3f
// Component : BarrelShifter
// Git hash  : 0eb84f39e3d12399d5296898ff7ae6fe856b0fc4

`timescale 1ns/1ps

module BarrelShifter (
  input  wire [31:0]   io_inpData,
  input  wire [31:0]   io_inpAmount,
  output wire [31:0]   io_outpData
);

  wire       [31:0]   temp_0;
  reg        [31:0]   temp_1;
  reg        [31:0]   temp_2;
  reg        [31:0]   temp_3;
  reg        [31:0]   temp_4;
  reg        [31:0]   temp_5;
  wire                when_miscBitwiseMods_l128;

  assign temp_0 = io_inpData;
  assign io_outpData = temp_5;
  assign when_miscBitwiseMods_l128 = (io_inpAmount[31 : 5] != 27'h0);
  always @(*) begin
    if(when_miscBitwiseMods_l128) begin
      temp_1 = 32'h0;
    end else begin
      temp_1 = (io_inpAmount[0] ? {temp_0[30 : 0],1'b0} : temp_0);
    end
  end

  always @(*) begin
    if(when_miscBitwiseMods_l128) begin
      temp_2 = 32'h0;
    end else begin
      temp_2 = (io_inpAmount[1] ? {temp_1[29 : 0],2'b00} : temp_1);
    end
  end

  always @(*) begin
    if(when_miscBitwiseMods_l128) begin
      temp_3 = 32'h0;
    end else begin
      temp_3 = (io_inpAmount[2] ? {temp_2[27 : 0],4'b0000} : temp_2);
    end
  end

  always @(*) begin
    if(when_miscBitwiseMods_l128) begin
      temp_4 = 32'h0;
    end else begin
      temp_4 = (io_inpAmount[3] ? {temp_3[23 : 0],8'h0} : temp_3);
    end
  end

  always @(*) begin
    if(when_miscBitwiseMods_l128) begin
      temp_5 = 32'h0;
    end else begin
      temp_5 = (io_inpAmount[4] ? {temp_4[15 : 0],16'h0} : temp_4);
    end
  end


endmodule
