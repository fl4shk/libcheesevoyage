module FpgacpuRamSimpleDualPortImpl_31 (
  input  wire          io_wrEn,
  input  wire [11:0]   io_wrAddr,
  input  wire [5:0]    io_wrData_colIdxVec_0,
  input  wire [5:0]    io_wrData_colIdxVec_1,
  input  wire [5:0]    io_wrData_colIdxVec_2,
  input  wire [5:0]    io_wrData_colIdxVec_3,
  input  wire [5:0]    io_wrData_colIdxVec_4,
  input  wire [5:0]    io_wrData_colIdxVec_5,
  input  wire [5:0]    io_wrData_colIdxVec_6,
  input  wire [5:0]    io_wrData_colIdxVec_7,
  input  wire [5:0]    io_wrData_colIdxVec_8,
  input  wire [5:0]    io_wrData_colIdxVec_9,
  input  wire [5:0]    io_wrData_colIdxVec_10,
  input  wire [5:0]    io_wrData_colIdxVec_11,
  input  wire [5:0]    io_wrData_colIdxVec_12,
  input  wire [5:0]    io_wrData_colIdxVec_13,
  input  wire [5:0]    io_wrData_colIdxVec_14,
  input  wire [5:0]    io_wrData_colIdxVec_15,
  input  wire          io_rdEn,
  input  wire [11:0]   io_rdAddr,
  output wire [5:0]    io_rdData_colIdxVec_0,
  output wire [5:0]    io_rdData_colIdxVec_1,
  output wire [5:0]    io_rdData_colIdxVec_2,
  output wire [5:0]    io_rdData_colIdxVec_3,
  output wire [5:0]    io_rdData_colIdxVec_4,
  output wire [5:0]    io_rdData_colIdxVec_5,
  output wire [5:0]    io_rdData_colIdxVec_6,
  output wire [5:0]    io_rdData_colIdxVec_7,
  output wire [5:0]    io_rdData_colIdxVec_8,
  output wire [5:0]    io_rdData_colIdxVec_9,
  output wire [5:0]    io_rdData_colIdxVec_10,
  output wire [5:0]    io_rdData_colIdxVec_11,
  output wire [5:0]    io_rdData_colIdxVec_12,
  output wire [5:0]    io_rdData_colIdxVec_13,
  output wire [5:0]    io_rdData_colIdxVec_14,
  output wire [5:0]    io_rdData_colIdxVec_15,
  input  wire          clk,
  input  wire          reset
);

  reg        [95:0]   arr_spinal_port1;
  wire       [95:0]   _zz_arr_port;
  wire       [95:0]   _zz_io_rdData_colIdxVec_0;
  (* ram_style = "block" , rw_addr_collision = "" *) reg [95:0] arr [0:4095];

  assign _zz_arr_port = {io_wrData_colIdxVec_15,{io_wrData_colIdxVec_14,{io_wrData_colIdxVec_13,{io_wrData_colIdxVec_12,{io_wrData_colIdxVec_11,{io_wrData_colIdxVec_10,{io_wrData_colIdxVec_9,{io_wrData_colIdxVec_8,{io_wrData_colIdxVec_7,{io_wrData_colIdxVec_6,{io_wrData_colIdxVec_5,{io_wrData_colIdxVec_4,{io_wrData_colIdxVec_3,{io_wrData_colIdxVec_2,{io_wrData_colIdxVec_1,io_wrData_colIdxVec_0}}}}}}}}}}}}}}};
  initial begin
    $readmemb("Gpu2dSimDut.v_toplevel_gpu2d_1_bgTileMemArr_0_impl_arr.bin",arr);
  end
  always @(posedge clk) begin
    if(io_wrEn) begin
      arr[io_wrAddr] <= _zz_arr_port;
    end
  end

  always @(posedge clk) begin
    if(io_rdEn) begin
      arr_spinal_port1 <= arr[io_rdAddr];
    end
  end

  assign _zz_io_rdData_colIdxVec_0 = arr_spinal_port1[95 : 0];
  assign io_rdData_colIdxVec_0 = _zz_io_rdData_colIdxVec_0[5 : 0];
  assign io_rdData_colIdxVec_1 = _zz_io_rdData_colIdxVec_0[11 : 6];
  assign io_rdData_colIdxVec_2 = _zz_io_rdData_colIdxVec_0[17 : 12];
  assign io_rdData_colIdxVec_3 = _zz_io_rdData_colIdxVec_0[23 : 18];
  assign io_rdData_colIdxVec_4 = _zz_io_rdData_colIdxVec_0[29 : 24];
  assign io_rdData_colIdxVec_5 = _zz_io_rdData_colIdxVec_0[35 : 30];
  assign io_rdData_colIdxVec_6 = _zz_io_rdData_colIdxVec_0[41 : 36];
  assign io_rdData_colIdxVec_7 = _zz_io_rdData_colIdxVec_0[47 : 42];
  assign io_rdData_colIdxVec_8 = _zz_io_rdData_colIdxVec_0[53 : 48];
  assign io_rdData_colIdxVec_9 = _zz_io_rdData_colIdxVec_0[59 : 54];
  assign io_rdData_colIdxVec_10 = _zz_io_rdData_colIdxVec_0[65 : 60];
  assign io_rdData_colIdxVec_11 = _zz_io_rdData_colIdxVec_0[71 : 66];
  assign io_rdData_colIdxVec_12 = _zz_io_rdData_colIdxVec_0[77 : 72];
  assign io_rdData_colIdxVec_13 = _zz_io_rdData_colIdxVec_0[83 : 78];
  assign io_rdData_colIdxVec_14 = _zz_io_rdData_colIdxVec_0[89 : 84];
  assign io_rdData_colIdxVec_15 = _zz_io_rdData_colIdxVec_0[95 : 90];

endmodule
