// Generator : SpinalHDL v1.9.3    git head : 029104c77a54c53f1edda327a3bea333f7d65fd9
// Component : unamed
// Git hash  : d3ce9b4f6f5fd2c803aa1a046614546d6232f49f

`timescale 1ns/1ps

module unamed (
  input               reset,
  input               clk
);


  Dut dut_1 (
    .reset (reset), //i
    .clk   (clk  )  //i
  );
  initial begin
    assume(reset); // lcvVgaGradientFormal.scala:L342
  end


endmodule

module Dut (
  input               reset,
  input               clk
);
  localparam LcvVgaState_front = 2'd0;
  localparam LcvVgaState_sync = 2'd1;
  localparam LcvVgaState_back = 2'd2;
  localparam LcvVgaState_visib = 2'd3;

  wire       [3:0]    vgaGrad_io_vgaCtrlIo_phys_col_r;
  wire       [3:0]    vgaGrad_io_vgaCtrlIo_phys_col_g;
  wire       [3:0]    vgaGrad_io_vgaCtrlIo_phys_col_b;
  wire                vgaGrad_io_vgaCtrlIo_phys_hsync;
  wire                vgaGrad_io_vgaCtrlIo_phys_vsync;
  wire       [1:0]    vgaGrad_io_vgaCtrlIo_misc_dbgHscS;
  wire       [9:0]    vgaGrad_io_vgaCtrlIo_misc_dbgHscC;
  wire       [1:0]    vgaGrad_io_vgaCtrlIo_misc_dbgHscNextS;
  wire       [1:0]    vgaGrad_io_vgaCtrlIo_misc_dbgVscS;
  wire       [8:0]    vgaGrad_io_vgaCtrlIo_misc_dbgVscC;
  wire       [1:0]    vgaGrad_io_vgaCtrlIo_misc_dbgVscNextS;
  wire                vgaGrad_io_vgaCtrlIo_misc_dbgFifoEmpty;
  wire                vgaGrad_io_vgaCtrlIo_misc_dbgFifoFull;
  wire                vgaGrad_io_vgaCtrlIo_misc_pixelEn;
  wire                vgaGrad_io_vgaCtrlIo_misc_nextVisib;
  wire                vgaGrad_io_vgaCtrlIo_misc_visib;
  wire                vgaGrad_io_vgaCtrlIo_misc_pastVisib;
  wire       [15:0]   vgaGrad_io_vgaCtrlIo_misc_drawPos_x;
  wire       [15:0]   vgaGrad_io_vgaCtrlIo_misc_drawPos_y;
  wire       [15:0]   vgaGrad_io_vgaCtrlIo_misc_pastDrawPos_x;
  wire       [15:0]   vgaGrad_io_vgaCtrlIo_misc_pastDrawPos_y;
  wire       [15:0]   vgaGrad_io_vgaCtrlIo_misc_size_x;
  wire       [15:0]   vgaGrad_io_vgaCtrlIo_misc_size_y;
  wire       [15:0]   vgaGrad_io_vidDitherIo_outpNextPos_x;
  wire       [15:0]   vgaGrad_io_vidDitherIo_outpNextPos_y;
  wire                vgaCtrl_io_push_ready;
  wire       [3:0]    vgaCtrl_io_phys_col_r;
  wire       [3:0]    vgaCtrl_io_phys_col_g;
  wire       [3:0]    vgaCtrl_io_phys_col_b;
  wire                vgaCtrl_io_phys_hsync;
  wire                vgaCtrl_io_phys_vsync;
  wire       [1:0]    vgaCtrl_io_misc_dbgHscS;
  wire       [9:0]    vgaCtrl_io_misc_dbgHscC;
  wire       [1:0]    vgaCtrl_io_misc_dbgHscNextS;
  wire       [1:0]    vgaCtrl_io_misc_dbgVscS;
  wire       [8:0]    vgaCtrl_io_misc_dbgVscC;
  wire       [1:0]    vgaCtrl_io_misc_dbgVscNextS;
  wire                vgaCtrl_io_misc_dbgFifoEmpty;
  wire                vgaCtrl_io_misc_dbgFifoFull;
  wire                vgaCtrl_io_misc_pixelEn;
  wire                vgaCtrl_io_misc_nextVisib;
  wire                vgaCtrl_io_misc_visib;
  wire                vgaCtrl_io_misc_pastVisib;
  wire       [15:0]   vgaCtrl_io_misc_drawPos_x;
  wire       [15:0]   vgaCtrl_io_misc_drawPos_y;
  wire       [15:0]   vgaCtrl_io_misc_pastDrawPos_x;
  wire       [15:0]   vgaCtrl_io_misc_pastDrawPos_y;
  wire       [15:0]   vgaCtrl_io_misc_size_x;
  wire       [15:0]   vgaCtrl_io_misc_size_y;
  wire                vidDith_io_push_ready;
  wire                vidDith_io_pop_valid;
  wire       [1:0]    vidDith_io_pop_payload_frameCnt;
  wire       [15:0]   vidDith_io_pop_payload_pos_x;
  wire       [15:0]   vidDith_io_pop_payload_pos_y;
  wire       [15:0]   vidDith_io_pop_payload_pastPos_x;
  wire       [15:0]   vidDith_io_pop_payload_pastPos_y;
  wire       [3:0]    vidDith_io_pop_payload_col_r;
  wire       [3:0]    vidDith_io_pop_payload_col_g;
  wire       [3:0]    vidDith_io_pop_payload_col_b;
  wire       [15:0]   vidDith_io_outpNextPos_x;
  wire       [15:0]   vidDith_io_outpNextPos_y;
  wire                vgaGrad_io_vgaCtrlIo_en;
  wire                vgaGrad_io_vgaCtrlIo_push_valid;
  wire       [3:0]    vgaGrad_io_vgaCtrlIo_push_payload_r;
  wire       [3:0]    vgaGrad_io_vgaCtrlIo_push_payload_g;
  wire       [3:0]    vgaGrad_io_vgaCtrlIo_push_payload_b;
  wire                vgaGrad_io_vidDitherIo_push_valid;
  wire       [5:0]    vgaGrad_io_vidDitherIo_push_payload_r;
  wire       [5:0]    vgaGrad_io_vidDitherIo_push_payload_g;
  wire       [5:0]    vgaGrad_io_vidDitherIo_push_payload_b;
  wire                vgaGrad_io_vidDitherIo_pop_ready;
  reg        [3:0]    ctrlPushFireCnt;
  reg        [3:0]    dithPushFireCnt;
  reg        [3:0]    dithPopFireCnt;
  wire                concurHscVisib;
  wire                concurVscVisib;
  wire                concurVisib;
  wire       [31:0]   allCtrlPushFireCnt;
  reg                 formal_with_past_after_reset;
  (* keep *) reg        [3:0]    _zz_1;
  wire                dut_1_vgaCtrl_io_push_fire;
  wire                when_lcvVgaGradientFormal_l230;
  wire                dut_1_vidDith_io_push_fire;
  wire                when_lcvVgaGradientFormal_l233;
  wire                dut_1_vidDith_io_pop_fire;
  wire                when_lcvVgaGradientFormal_l236;

  LcvVgaCtrl vgaCtrl (
    .io_en                 (vgaGrad_io_vgaCtrlIo_en                 ), //i
    .io_push_valid         (vgaGrad_io_vgaCtrlIo_push_valid         ), //i
    .io_push_ready         (vgaCtrl_io_push_ready                   ), //o
    .io_push_payload_r     (vgaGrad_io_vgaCtrlIo_push_payload_r[3:0]), //i
    .io_push_payload_g     (vgaGrad_io_vgaCtrlIo_push_payload_g[3:0]), //i
    .io_push_payload_b     (vgaGrad_io_vgaCtrlIo_push_payload_b[3:0]), //i
    .io_phys_col_r         (vgaCtrl_io_phys_col_r[3:0]              ), //o
    .io_phys_col_g         (vgaCtrl_io_phys_col_g[3:0]              ), //o
    .io_phys_col_b         (vgaCtrl_io_phys_col_b[3:0]              ), //o
    .io_phys_hsync         (vgaCtrl_io_phys_hsync                   ), //o
    .io_phys_vsync         (vgaCtrl_io_phys_vsync                   ), //o
    .io_misc_dbgHscS       (vgaCtrl_io_misc_dbgHscS[1:0]            ), //o
    .io_misc_dbgHscC       (vgaCtrl_io_misc_dbgHscC[9:0]            ), //o
    .io_misc_dbgHscNextS   (vgaCtrl_io_misc_dbgHscNextS[1:0]        ), //o
    .io_misc_dbgVscS       (vgaCtrl_io_misc_dbgVscS[1:0]            ), //o
    .io_misc_dbgVscC       (vgaCtrl_io_misc_dbgVscC[8:0]            ), //o
    .io_misc_dbgVscNextS   (vgaCtrl_io_misc_dbgVscNextS[1:0]        ), //o
    .io_misc_dbgFifoEmpty  (vgaCtrl_io_misc_dbgFifoEmpty            ), //o
    .io_misc_dbgFifoFull   (vgaCtrl_io_misc_dbgFifoFull             ), //o
    .io_misc_pixelEn       (vgaCtrl_io_misc_pixelEn                 ), //o
    .io_misc_nextVisib     (vgaCtrl_io_misc_nextVisib               ), //o
    .io_misc_visib         (vgaCtrl_io_misc_visib                   ), //o
    .io_misc_pastVisib     (vgaCtrl_io_misc_pastVisib               ), //o
    .io_misc_drawPos_x     (vgaCtrl_io_misc_drawPos_x[15:0]         ), //o
    .io_misc_drawPos_y     (vgaCtrl_io_misc_drawPos_y[15:0]         ), //o
    .io_misc_pastDrawPos_x (vgaCtrl_io_misc_pastDrawPos_x[15:0]     ), //o
    .io_misc_pastDrawPos_y (vgaCtrl_io_misc_pastDrawPos_y[15:0]     ), //o
    .io_misc_size_x        (vgaCtrl_io_misc_size_x[15:0]            ), //o
    .io_misc_size_y        (vgaCtrl_io_misc_size_y[15:0]            ), //o
    .reset                 (reset                                   ), //i
    .clk                   (clk                                     )  //i
  );
  LcvVideoDitherer vidDith (
    .io_push_valid            (vgaGrad_io_vidDitherIo_push_valid         ), //i
    .io_push_ready            (vidDith_io_push_ready                     ), //o
    .io_push_payload_r        (vgaGrad_io_vidDitherIo_push_payload_r[5:0]), //i
    .io_push_payload_g        (vgaGrad_io_vidDitherIo_push_payload_g[5:0]), //i
    .io_push_payload_b        (vgaGrad_io_vidDitherIo_push_payload_b[5:0]), //i
    .io_pop_valid             (vidDith_io_pop_valid                      ), //o
    .io_pop_ready             (vgaGrad_io_vidDitherIo_pop_ready          ), //i
    .io_pop_payload_frameCnt  (vidDith_io_pop_payload_frameCnt[1:0]      ), //o
    .io_pop_payload_pos_x     (vidDith_io_pop_payload_pos_x[15:0]        ), //o
    .io_pop_payload_pos_y     (vidDith_io_pop_payload_pos_y[15:0]        ), //o
    .io_pop_payload_pastPos_x (vidDith_io_pop_payload_pastPos_x[15:0]    ), //o
    .io_pop_payload_pastPos_y (vidDith_io_pop_payload_pastPos_y[15:0]    ), //o
    .io_pop_payload_col_r     (vidDith_io_pop_payload_col_r[3:0]         ), //o
    .io_pop_payload_col_g     (vidDith_io_pop_payload_col_g[3:0]         ), //o
    .io_pop_payload_col_b     (vidDith_io_pop_payload_col_b[3:0]         ), //o
    .io_outpNextPos_x         (vidDith_io_outpNextPos_x[15:0]            ), //o
    .io_outpNextPos_y         (vidDith_io_outpNextPos_y[15:0]            ), //o
    .reset                    (reset                                     ), //i
    .clk                      (clk                                       )  //i
  );
  LcvVgaGradient vgaGrad (
    .io_vgaCtrlIo_en                      (vgaGrad_io_vgaCtrlIo_en                      ), //o
    .io_vgaCtrlIo_push_valid              (vgaGrad_io_vgaCtrlIo_push_valid              ), //o
    .io_vgaCtrlIo_push_ready              (vgaCtrl_io_push_ready                        ), //i
    .io_vgaCtrlIo_push_payload_r          (vgaGrad_io_vgaCtrlIo_push_payload_r[3:0]     ), //o
    .io_vgaCtrlIo_push_payload_g          (vgaGrad_io_vgaCtrlIo_push_payload_g[3:0]     ), //o
    .io_vgaCtrlIo_push_payload_b          (vgaGrad_io_vgaCtrlIo_push_payload_b[3:0]     ), //o
    .io_vgaCtrlIo_phys_col_r              (vgaGrad_io_vgaCtrlIo_phys_col_r[3:0]         ), //i
    .io_vgaCtrlIo_phys_col_g              (vgaGrad_io_vgaCtrlIo_phys_col_g[3:0]         ), //i
    .io_vgaCtrlIo_phys_col_b              (vgaGrad_io_vgaCtrlIo_phys_col_b[3:0]         ), //i
    .io_vgaCtrlIo_phys_hsync              (vgaGrad_io_vgaCtrlIo_phys_hsync              ), //i
    .io_vgaCtrlIo_phys_vsync              (vgaGrad_io_vgaCtrlIo_phys_vsync              ), //i
    .io_vgaCtrlIo_misc_dbgHscS            (vgaGrad_io_vgaCtrlIo_misc_dbgHscS[1:0]       ), //i
    .io_vgaCtrlIo_misc_dbgHscC            (vgaGrad_io_vgaCtrlIo_misc_dbgHscC[9:0]       ), //i
    .io_vgaCtrlIo_misc_dbgHscNextS        (vgaGrad_io_vgaCtrlIo_misc_dbgHscNextS[1:0]   ), //i
    .io_vgaCtrlIo_misc_dbgVscS            (vgaGrad_io_vgaCtrlIo_misc_dbgVscS[1:0]       ), //i
    .io_vgaCtrlIo_misc_dbgVscC            (vgaGrad_io_vgaCtrlIo_misc_dbgVscC[8:0]       ), //i
    .io_vgaCtrlIo_misc_dbgVscNextS        (vgaGrad_io_vgaCtrlIo_misc_dbgVscNextS[1:0]   ), //i
    .io_vgaCtrlIo_misc_dbgFifoEmpty       (vgaGrad_io_vgaCtrlIo_misc_dbgFifoEmpty       ), //i
    .io_vgaCtrlIo_misc_dbgFifoFull        (vgaGrad_io_vgaCtrlIo_misc_dbgFifoFull        ), //i
    .io_vgaCtrlIo_misc_pixelEn            (vgaGrad_io_vgaCtrlIo_misc_pixelEn            ), //i
    .io_vgaCtrlIo_misc_nextVisib          (vgaGrad_io_vgaCtrlIo_misc_nextVisib          ), //i
    .io_vgaCtrlIo_misc_visib              (vgaGrad_io_vgaCtrlIo_misc_visib              ), //i
    .io_vgaCtrlIo_misc_pastVisib          (vgaGrad_io_vgaCtrlIo_misc_pastVisib          ), //i
    .io_vgaCtrlIo_misc_drawPos_x          (vgaGrad_io_vgaCtrlIo_misc_drawPos_x[15:0]    ), //i
    .io_vgaCtrlIo_misc_drawPos_y          (vgaGrad_io_vgaCtrlIo_misc_drawPos_y[15:0]    ), //i
    .io_vgaCtrlIo_misc_pastDrawPos_x      (vgaGrad_io_vgaCtrlIo_misc_pastDrawPos_x[15:0]), //i
    .io_vgaCtrlIo_misc_pastDrawPos_y      (vgaGrad_io_vgaCtrlIo_misc_pastDrawPos_y[15:0]), //i
    .io_vgaCtrlIo_misc_size_x             (vgaGrad_io_vgaCtrlIo_misc_size_x[15:0]       ), //i
    .io_vgaCtrlIo_misc_size_y             (vgaGrad_io_vgaCtrlIo_misc_size_y[15:0]       ), //i
    .io_vidDitherIo_push_valid            (vgaGrad_io_vidDitherIo_push_valid            ), //o
    .io_vidDitherIo_push_ready            (vidDith_io_push_ready                        ), //i
    .io_vidDitherIo_push_payload_r        (vgaGrad_io_vidDitherIo_push_payload_r[5:0]   ), //o
    .io_vidDitherIo_push_payload_g        (vgaGrad_io_vidDitherIo_push_payload_g[5:0]   ), //o
    .io_vidDitherIo_push_payload_b        (vgaGrad_io_vidDitherIo_push_payload_b[5:0]   ), //o
    .io_vidDitherIo_pop_valid             (vidDith_io_pop_valid                         ), //i
    .io_vidDitherIo_pop_ready             (vgaGrad_io_vidDitherIo_pop_ready             ), //o
    .io_vidDitherIo_pop_payload_frameCnt  (vidDith_io_pop_payload_frameCnt[1:0]         ), //i
    .io_vidDitherIo_pop_payload_pos_x     (vidDith_io_pop_payload_pos_x[15:0]           ), //i
    .io_vidDitherIo_pop_payload_pos_y     (vidDith_io_pop_payload_pos_y[15:0]           ), //i
    .io_vidDitherIo_pop_payload_pastPos_x (vidDith_io_pop_payload_pastPos_x[15:0]       ), //i
    .io_vidDitherIo_pop_payload_pastPos_y (vidDith_io_pop_payload_pastPos_y[15:0]       ), //i
    .io_vidDitherIo_pop_payload_col_r     (vidDith_io_pop_payload_col_r[3:0]            ), //i
    .io_vidDitherIo_pop_payload_col_g     (vidDith_io_pop_payload_col_g[3:0]            ), //i
    .io_vidDitherIo_pop_payload_col_b     (vidDith_io_pop_payload_col_b[3:0]            ), //i
    .io_vidDitherIo_outpNextPos_x         (vgaGrad_io_vidDitherIo_outpNextPos_x[15:0]   ), //i
    .io_vidDitherIo_outpNextPos_y         (vgaGrad_io_vidDitherIo_outpNextPos_y[15:0]   ), //i
    .reset                                (reset                                        ), //i
    .clk                                  (clk                                          )  //i
  );
  initial begin
    assume(reset); // lcvVgaGradientFormal.scala:L47
    assume((ctrlPushFireCnt == 4'b0000)); // lcvVgaGradientFormal.scala:L106
    assume((dithPushFireCnt == 4'b0000)); // lcvVgaGradientFormal.scala:L109
    assume((dithPopFireCnt == 4'b0000)); // lcvVgaGradientFormal.scala:L112
  end

  assign concurHscVisib = (vgaCtrl_io_misc_dbgHscS == LcvVgaState_visib);
  assign concurVscVisib = (vgaCtrl_io_misc_dbgVscS == LcvVgaState_visib);
  assign concurVisib = (concurHscVisib && concurVscVisib);
  assign allCtrlPushFireCnt = 32'h00000000;
  assign dut_1_vgaCtrl_io_push_fire = (vgaGrad_io_vgaCtrlIo_push_valid && vgaCtrl_io_push_ready);
  assign when_lcvVgaGradientFormal_l230 = ((! concurVisib) || dut_1_vgaCtrl_io_push_fire);
  assign dut_1_vidDith_io_push_fire = (vgaGrad_io_vidDitherIo_push_valid && vidDith_io_push_ready);
  assign when_lcvVgaGradientFormal_l233 = ((! concurVisib) || dut_1_vidDith_io_push_fire);
  assign dut_1_vidDith_io_pop_fire = (vidDith_io_pop_valid && vgaGrad_io_vidDitherIo_pop_ready);
  assign when_lcvVgaGradientFormal_l236 = ((! concurVisib) || dut_1_vidDith_io_pop_fire);
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      ctrlPushFireCnt <= 4'b0000;
      dithPushFireCnt <= 4'b0000;
      dithPopFireCnt <= 4'b0000;
      formal_with_past_after_reset <= 1'b0;
    end else begin
      formal_with_past_after_reset <= (! reset);
      if(formal_with_past_after_reset) begin
        cover((vgaGrad_io_vgaCtrlIo_push_payload_r != 4'b0000)); // lcvVgaGradientFormal.scala:L184
        cover(vgaCtrl_io_phys_hsync); // lcvVgaGradientFormal.scala:L185
        cover(vgaCtrl_io_phys_vsync); // lcvVgaGradientFormal.scala:L186
        if(concurVisib) begin
          cover(((_zz_1[0] == 1'b1) || (_zz_1[0] == 1'b0))); // lcvVgaGradientFormal.scala:L198
          ctrlPushFireCnt <= (ctrlPushFireCnt + 4'b0001);
          dithPushFireCnt <= (dithPushFireCnt + 4'b0001);
          dithPopFireCnt <= (dithPopFireCnt + 4'b0001);
          assert((ctrlPushFireCnt < 4'b0110)); // lcvVgaGradientFormal.scala:L225
          assert((dithPushFireCnt < 4'b0110)); // lcvVgaGradientFormal.scala:L226
          assert((dithPopFireCnt < 4'b0110)); // lcvVgaGradientFormal.scala:L227
        end
        if(when_lcvVgaGradientFormal_l230) begin
          ctrlPushFireCnt <= 4'b0000;
        end
        if(when_lcvVgaGradientFormal_l233) begin
          dithPushFireCnt <= 4'b0000;
        end
        if(when_lcvVgaGradientFormal_l236) begin
          dithPopFireCnt <= 4'b0000;
        end
      end
    end
  end

  always @(posedge clk or posedge reset) begin
    if(reset) begin
      _zz_1 <= 4'b0000;
    end else begin
      if(concurVisib) begin
        _zz_1 <= vgaCtrl_io_phys_col_r;
      end
    end
  end


endmodule

module LcvVgaGradient (
  output              io_vgaCtrlIo_en,
  output              io_vgaCtrlIo_push_valid,
  input               io_vgaCtrlIo_push_ready,
  output     [3:0]    io_vgaCtrlIo_push_payload_r,
  output     [3:0]    io_vgaCtrlIo_push_payload_g,
  output     [3:0]    io_vgaCtrlIo_push_payload_b,
  input      [3:0]    io_vgaCtrlIo_phys_col_r,
  input      [3:0]    io_vgaCtrlIo_phys_col_g,
  input      [3:0]    io_vgaCtrlIo_phys_col_b,
  input               io_vgaCtrlIo_phys_hsync,
  input               io_vgaCtrlIo_phys_vsync,
  input      [1:0]    io_vgaCtrlIo_misc_dbgHscS,
  input      [9:0]    io_vgaCtrlIo_misc_dbgHscC,
  input      [1:0]    io_vgaCtrlIo_misc_dbgHscNextS,
  input      [1:0]    io_vgaCtrlIo_misc_dbgVscS,
  input      [8:0]    io_vgaCtrlIo_misc_dbgVscC,
  input      [1:0]    io_vgaCtrlIo_misc_dbgVscNextS,
  input               io_vgaCtrlIo_misc_dbgFifoEmpty,
  input               io_vgaCtrlIo_misc_dbgFifoFull,
  input               io_vgaCtrlIo_misc_pixelEn,
  input               io_vgaCtrlIo_misc_nextVisib,
  input               io_vgaCtrlIo_misc_visib,
  input               io_vgaCtrlIo_misc_pastVisib,
  input      [15:0]   io_vgaCtrlIo_misc_drawPos_x,
  input      [15:0]   io_vgaCtrlIo_misc_drawPos_y,
  input      [15:0]   io_vgaCtrlIo_misc_pastDrawPos_x,
  input      [15:0]   io_vgaCtrlIo_misc_pastDrawPos_y,
  input      [15:0]   io_vgaCtrlIo_misc_size_x,
  input      [15:0]   io_vgaCtrlIo_misc_size_y,
  output              io_vidDitherIo_push_valid,
  input               io_vidDitherIo_push_ready,
  output     [5:0]    io_vidDitherIo_push_payload_r,
  output     [5:0]    io_vidDitherIo_push_payload_g,
  output     [5:0]    io_vidDitherIo_push_payload_b,
  input               io_vidDitherIo_pop_valid,
  output              io_vidDitherIo_pop_ready,
  input      [1:0]    io_vidDitherIo_pop_payload_frameCnt,
  input      [15:0]   io_vidDitherIo_pop_payload_pos_x,
  input      [15:0]   io_vidDitherIo_pop_payload_pos_y,
  input      [15:0]   io_vidDitherIo_pop_payload_pastPos_x,
  input      [15:0]   io_vidDitherIo_pop_payload_pastPos_y,
  input      [3:0]    io_vidDitherIo_pop_payload_col_r,
  input      [3:0]    io_vidDitherIo_pop_payload_col_g,
  input      [3:0]    io_vidDitherIo_pop_payload_col_b,
  input      [15:0]   io_vidDitherIo_outpNextPos_x,
  input      [15:0]   io_vidDitherIo_outpNextPos_y,
  input               reset,
  input               clk
);
  localparam LcvVgaState_front = 2'd0;
  localparam LcvVgaState_sync = 2'd1;
  localparam LcvVgaState_back = 2'd2;
  localparam LcvVgaState_visib = 2'd3;

  wire                skidBuf_io_misc_clear;
  wire                skidBuf_io_next_valid;
  wire       [5:0]    skidBuf_io_next_payload_r;
  wire       [5:0]    skidBuf_io_next_payload_g;
  wire       [5:0]    skidBuf_io_next_payload_b;
  wire                skidBuf_io_prev_ready;
  wire                sbPrevFire;
  reg        [5:0]    rPastCol_r;
  reg        [5:0]    rPastCol_g;
  reg        [5:0]    rPastCol_b;
  `ifndef SYNTHESIS
  reg [39:0] io_vgaCtrlIo_misc_dbgHscS_string;
  reg [39:0] io_vgaCtrlIo_misc_dbgHscNextS_string;
  reg [39:0] io_vgaCtrlIo_misc_dbgVscS_string;
  reg [39:0] io_vgaCtrlIo_misc_dbgVscNextS_string;
  `endif


  PipeSkidBuf skidBuf (
    .io_next_valid     (skidBuf_io_next_valid         ), //o
    .io_next_ready     (io_vidDitherIo_push_ready     ), //i
    .io_next_payload_r (skidBuf_io_next_payload_r[5:0]), //o
    .io_next_payload_g (skidBuf_io_next_payload_g[5:0]), //o
    .io_next_payload_b (skidBuf_io_next_payload_b[5:0]), //o
    .io_prev_valid     (1'b1                          ), //i
    .io_prev_ready     (skidBuf_io_prev_ready         ), //o
    .io_prev_payload_r (6'h3f                         ), //i
    .io_prev_payload_g (6'h00                         ), //i
    .io_prev_payload_b (6'h00                         ), //i
    .io_misc_clear     (skidBuf_io_misc_clear         ), //i
    .reset             (reset                         ), //i
    .clk               (clk                           )  //i
  );
  `ifndef SYNTHESIS
  always @(*) begin
    case(io_vgaCtrlIo_misc_dbgHscS)
      LcvVgaState_front : io_vgaCtrlIo_misc_dbgHscS_string = "front";
      LcvVgaState_sync : io_vgaCtrlIo_misc_dbgHscS_string = "sync ";
      LcvVgaState_back : io_vgaCtrlIo_misc_dbgHscS_string = "back ";
      LcvVgaState_visib : io_vgaCtrlIo_misc_dbgHscS_string = "visib";
      default : io_vgaCtrlIo_misc_dbgHscS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_vgaCtrlIo_misc_dbgHscNextS)
      LcvVgaState_front : io_vgaCtrlIo_misc_dbgHscNextS_string = "front";
      LcvVgaState_sync : io_vgaCtrlIo_misc_dbgHscNextS_string = "sync ";
      LcvVgaState_back : io_vgaCtrlIo_misc_dbgHscNextS_string = "back ";
      LcvVgaState_visib : io_vgaCtrlIo_misc_dbgHscNextS_string = "visib";
      default : io_vgaCtrlIo_misc_dbgHscNextS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_vgaCtrlIo_misc_dbgVscS)
      LcvVgaState_front : io_vgaCtrlIo_misc_dbgVscS_string = "front";
      LcvVgaState_sync : io_vgaCtrlIo_misc_dbgVscS_string = "sync ";
      LcvVgaState_back : io_vgaCtrlIo_misc_dbgVscS_string = "back ";
      LcvVgaState_visib : io_vgaCtrlIo_misc_dbgVscS_string = "visib";
      default : io_vgaCtrlIo_misc_dbgVscS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_vgaCtrlIo_misc_dbgVscNextS)
      LcvVgaState_front : io_vgaCtrlIo_misc_dbgVscNextS_string = "front";
      LcvVgaState_sync : io_vgaCtrlIo_misc_dbgVscNextS_string = "sync ";
      LcvVgaState_back : io_vgaCtrlIo_misc_dbgVscNextS_string = "back ";
      LcvVgaState_visib : io_vgaCtrlIo_misc_dbgVscNextS_string = "visib";
      default : io_vgaCtrlIo_misc_dbgVscNextS_string = "?????";
    endcase
  end
  `endif

  assign sbPrevFire = (1'b1 && skidBuf_io_prev_ready);
  assign io_vidDitherIo_push_valid = skidBuf_io_next_valid;
  assign io_vidDitherIo_push_payload_r = skidBuf_io_next_payload_r;
  assign io_vidDitherIo_push_payload_g = skidBuf_io_next_payload_g;
  assign io_vidDitherIo_push_payload_b = skidBuf_io_next_payload_b;
  assign io_vgaCtrlIo_push_valid = io_vidDitherIo_pop_valid;
  assign io_vgaCtrlIo_push_payload_r = io_vidDitherIo_pop_payload_col_r;
  assign io_vgaCtrlIo_push_payload_g = io_vidDitherIo_pop_payload_col_g;
  assign io_vgaCtrlIo_push_payload_b = io_vidDitherIo_pop_payload_col_b;
  assign io_vidDitherIo_pop_ready = io_vgaCtrlIo_push_ready;
  assign io_vgaCtrlIo_en = 1'b1;
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      rPastCol_r <= 6'h00;
      rPastCol_g <= 6'h00;
      rPastCol_b <= 6'h00;
    end else begin
      rPastCol_r <= 6'h3f;
      rPastCol_g <= 6'h00;
      rPastCol_b <= 6'h00;
    end
  end


endmodule

module LcvVideoDitherer (
  input               io_push_valid,
  output              io_push_ready,
  input      [5:0]    io_push_payload_r,
  input      [5:0]    io_push_payload_g,
  input      [5:0]    io_push_payload_b,
  output              io_pop_valid,
  input               io_pop_ready,
  output     [1:0]    io_pop_payload_frameCnt,
  output     [15:0]   io_pop_payload_pos_x,
  output     [15:0]   io_pop_payload_pos_y,
  output     [15:0]   io_pop_payload_pastPos_x,
  output     [15:0]   io_pop_payload_pastPos_y,
  output     [3:0]    io_pop_payload_col_r,
  output     [3:0]    io_pop_payload_col_g,
  output     [3:0]    io_pop_payload_col_b,
  output reg [15:0]   io_outpNextPos_x,
  output reg [15:0]   io_outpNextPos_y,
  input               reset,
  input               clk
);

  wire       [1:0]    fifo_io_push_payload_frameCnt;
  reg        [3:0]    fifo_io_push_payload_col_r;
  reg        [3:0]    fifo_io_push_payload_col_g;
  reg        [3:0]    fifo_io_push_payload_col_b;
  wire                fifo_io_push_ready;
  wire                fifo_io_pop_valid;
  wire       [1:0]    fifo_io_pop_payload_frameCnt;
  wire       [15:0]   fifo_io_pop_payload_pos_x;
  wire       [15:0]   fifo_io_pop_payload_pos_y;
  wire       [15:0]   fifo_io_pop_payload_pastPos_x;
  wire       [15:0]   fifo_io_pop_payload_pastPos_y;
  wire       [3:0]    fifo_io_pop_payload_col_r;
  wire       [3:0]    fifo_io_pop_payload_col_g;
  wire       [3:0]    fifo_io_pop_payload_col_b;
  wire                fifo_io_misc_empty;
  wire                fifo_io_misc_full;
  reg        [1:0]    _zz_chanDelta_1;
  wire       [0:0]    _zz_chanDelta_2;
  reg        [1:0]    _zz_chanDelta_3;
  reg        [1:0]    _zz_chanDelta_4;
  reg        [1:0]    _zz_chanDelta_5;
  reg        [1:0]    _zz_chanDelta_6;
  reg        [1:0]    _zz_chanDelta_7;
  reg        [1:0]    _zz_chanDelta_8;
  wire       [5:0]    _zz_colInPlusDelta_r;
  wire       [5:0]    _zz_colInPlusDelta_r_1;
  wire       [5:0]    _zz_colInPlusDelta_g;
  wire       [5:0]    _zz_colInPlusDelta_g_1;
  wire       [5:0]    _zz_colInPlusDelta_b;
  wire       [5:0]    _zz_colInPlusDelta_b_1;
  wire       [1:0]    pattern_0_0_0;
  wire       [1:0]    pattern_0_0_1;
  wire       [1:0]    pattern_0_1_0;
  wire       [1:0]    pattern_0_1_1;
  wire       [1:0]    pattern_1_0_0;
  wire       [1:0]    pattern_1_0_1;
  wire       [1:0]    pattern_1_1_0;
  wire       [1:0]    pattern_1_1_1;
  wire       [1:0]    pattern_2_0_0;
  wire       [1:0]    pattern_2_0_1;
  wire       [1:0]    pattern_2_1_0;
  wire       [1:0]    pattern_2_1_1;
  wire       [1:0]    pattern_3_0_0;
  wire       [1:0]    pattern_3_0_1;
  wire       [1:0]    pattern_3_1_0;
  wire       [1:0]    pattern_3_1_1;
  reg        [1:0]    rPastTempPayload_frameCnt;
  reg        [15:0]   rPastTempPayload_pos_x;
  reg        [15:0]   rPastTempPayload_pos_y;
  reg        [15:0]   rPastTempPayload_pastPos_x;
  reg        [15:0]   rPastTempPayload_pastPos_y;
  reg        [3:0]    rPastTempPayload_col_r;
  reg        [3:0]    rPastTempPayload_col_g;
  reg        [3:0]    rPastTempPayload_col_b;
  wire                io_push_fire;
  wire       [15:0]   posPlus1_x;
  wire       [15:0]   posPlus1_y;
  reg        [5:0]    dicol_r;
  reg        [5:0]    dicol_g;
  reg        [5:0]    dicol_b;
  wire       [0:0]    _zz_chanDelta;
  wire       [1:0]    chanDelta;
  reg        [6:0]    colInPlusDelta_r;
  reg        [6:0]    colInPlusDelta_g;
  reg        [6:0]    colInPlusDelta_b;
  reg        [15:0]   rPastTempPayloadPosY;
  wire                when_lcvVideoDithererMod_l274;
  wire                when_lcvVideoDithererMod_l284;
  wire                when_lcvVideoDithererMod_l331;
  wire                when_lcvVideoDithererMod_l345;
  wire                when_lcvVideoDithererMod_l359;

  assign _zz_colInPlusDelta_r = (io_push_payload_r + _zz_colInPlusDelta_r_1);
  assign _zz_colInPlusDelta_r_1 = {4'd0, chanDelta};
  assign _zz_colInPlusDelta_g = (io_push_payload_g + _zz_colInPlusDelta_g_1);
  assign _zz_colInPlusDelta_g_1 = {4'd0, chanDelta};
  assign _zz_colInPlusDelta_b = (io_push_payload_b + _zz_colInPlusDelta_b_1);
  assign _zz_colInPlusDelta_b_1 = {4'd0, chanDelta};
  assign _zz_chanDelta_2 = rPastTempPayload_pos_x[0 : 0];
  AsyncReadFifo fifo (
    .io_push_valid             (io_push_valid                      ), //i
    .io_push_ready             (fifo_io_push_ready                 ), //o
    .io_push_payload_frameCnt  (fifo_io_push_payload_frameCnt[1:0] ), //i
    .io_push_payload_pos_x     (io_outpNextPos_x[15:0]             ), //i
    .io_push_payload_pos_y     (io_outpNextPos_y[15:0]             ), //i
    .io_push_payload_pastPos_x (rPastTempPayload_pos_x[15:0]       ), //i
    .io_push_payload_pastPos_y (rPastTempPayload_pos_y[15:0]       ), //i
    .io_push_payload_col_r     (fifo_io_push_payload_col_r[3:0]    ), //i
    .io_push_payload_col_g     (fifo_io_push_payload_col_g[3:0]    ), //i
    .io_push_payload_col_b     (fifo_io_push_payload_col_b[3:0]    ), //i
    .io_pop_valid              (fifo_io_pop_valid                  ), //o
    .io_pop_ready              (io_pop_ready                       ), //i
    .io_pop_payload_frameCnt   (fifo_io_pop_payload_frameCnt[1:0]  ), //o
    .io_pop_payload_pos_x      (fifo_io_pop_payload_pos_x[15:0]    ), //o
    .io_pop_payload_pos_y      (fifo_io_pop_payload_pos_y[15:0]    ), //o
    .io_pop_payload_pastPos_x  (fifo_io_pop_payload_pastPos_x[15:0]), //o
    .io_pop_payload_pastPos_y  (fifo_io_pop_payload_pastPos_y[15:0]), //o
    .io_pop_payload_col_r      (fifo_io_pop_payload_col_r[3:0]     ), //o
    .io_pop_payload_col_g      (fifo_io_pop_payload_col_g[3:0]     ), //o
    .io_pop_payload_col_b      (fifo_io_pop_payload_col_b[3:0]     ), //o
    .io_misc_empty             (fifo_io_misc_empty                 ), //o
    .io_misc_full              (fifo_io_misc_full                  ), //o
    .reset                     (reset                              ), //i
    .clk                       (clk                                )  //i
  );
  always @(*) begin
    case(_zz_chanDelta_2)
      1'b0 : _zz_chanDelta_1 = _zz_chanDelta_3;
      default : _zz_chanDelta_1 = _zz_chanDelta_6;
    endcase
  end

  always @(*) begin
    case(_zz_chanDelta)
      1'b0 : begin
        _zz_chanDelta_3 = _zz_chanDelta_4;
        _zz_chanDelta_6 = _zz_chanDelta_7;
      end
      default : begin
        _zz_chanDelta_3 = _zz_chanDelta_5;
        _zz_chanDelta_6 = _zz_chanDelta_8;
      end
    endcase
  end

  always @(*) begin
    case(rPastTempPayload_frameCnt)
      2'b00 : begin
        _zz_chanDelta_4 = pattern_0_0_0;
        _zz_chanDelta_5 = pattern_0_1_0;
        _zz_chanDelta_7 = pattern_0_0_1;
        _zz_chanDelta_8 = pattern_0_1_1;
      end
      2'b01 : begin
        _zz_chanDelta_4 = pattern_1_0_0;
        _zz_chanDelta_5 = pattern_1_1_0;
        _zz_chanDelta_7 = pattern_1_0_1;
        _zz_chanDelta_8 = pattern_1_1_1;
      end
      2'b10 : begin
        _zz_chanDelta_4 = pattern_2_0_0;
        _zz_chanDelta_5 = pattern_2_1_0;
        _zz_chanDelta_7 = pattern_2_0_1;
        _zz_chanDelta_8 = pattern_2_1_1;
      end
      default : begin
        _zz_chanDelta_4 = pattern_3_0_0;
        _zz_chanDelta_5 = pattern_3_1_0;
        _zz_chanDelta_7 = pattern_3_0_1;
        _zz_chanDelta_8 = pattern_3_1_1;
      end
    endcase
  end

  assign pattern_0_0_0 = 2'b00;
  assign pattern_0_0_1 = 2'b01;
  assign pattern_0_1_0 = 2'b11;
  assign pattern_0_1_1 = 2'b10;
  assign pattern_1_0_0 = 2'b01;
  assign pattern_1_0_1 = 2'b00;
  assign pattern_1_1_0 = 2'b10;
  assign pattern_1_1_1 = 2'b11;
  assign pattern_2_0_0 = 2'b11;
  assign pattern_2_0_1 = 2'b10;
  assign pattern_2_1_0 = 2'b00;
  assign pattern_2_1_1 = 2'b01;
  assign pattern_3_0_0 = 2'b10;
  assign pattern_3_0_1 = 2'b11;
  assign pattern_3_1_0 = 2'b01;
  assign pattern_3_1_1 = 2'b00;
  assign io_push_ready = fifo_io_push_ready;
  assign io_pop_valid = fifo_io_pop_valid;
  assign io_pop_payload_frameCnt = fifo_io_pop_payload_frameCnt;
  assign io_pop_payload_pos_x = fifo_io_pop_payload_pos_x;
  assign io_pop_payload_pos_y = fifo_io_pop_payload_pos_y;
  assign io_pop_payload_pastPos_x = fifo_io_pop_payload_pastPos_x;
  assign io_pop_payload_pastPos_y = fifo_io_pop_payload_pastPos_y;
  assign io_pop_payload_col_r = fifo_io_pop_payload_col_r;
  assign io_pop_payload_col_g = fifo_io_pop_payload_col_g;
  assign io_pop_payload_col_b = fifo_io_pop_payload_col_b;
  assign io_push_fire = (io_push_valid && io_push_ready);
  assign posPlus1_x = (io_pop_payload_pos_x + 16'h0001);
  assign posPlus1_y = (io_pop_payload_pos_y + 16'h0001);
  assign _zz_chanDelta = rPastTempPayload_pos_y[0 : 0];
  assign chanDelta = _zz_chanDelta_1;
  assign when_lcvVideoDithererMod_l274 = (posPlus1_x < 16'h0280);
  always @(*) begin
    if(when_lcvVideoDithererMod_l274) begin
      io_outpNextPos_x = posPlus1_x;
    end else begin
      io_outpNextPos_x = 16'h0000;
    end
  end

  always @(*) begin
    if(when_lcvVideoDithererMod_l274) begin
      io_outpNextPos_y = rPastTempPayloadPosY;
    end else begin
      if(when_lcvVideoDithererMod_l284) begin
        io_outpNextPos_y = posPlus1_y;
      end else begin
        io_outpNextPos_y = 16'h0000;
      end
    end
  end

  assign when_lcvVideoDithererMod_l284 = (posPlus1_y < 16'h01e0);
  assign fifo_io_push_payload_frameCnt = (rPastTempPayload_frameCnt + 2'b01);
  always @(*) begin
    if(io_push_fire) begin
      colInPlusDelta_r = {1'd0, _zz_colInPlusDelta_r};
    end else begin
      colInPlusDelta_r = 7'h00;
    end
  end

  always @(*) begin
    if(io_push_fire) begin
      colInPlusDelta_g = {1'd0, _zz_colInPlusDelta_g};
    end else begin
      colInPlusDelta_g = 7'h00;
    end
  end

  always @(*) begin
    if(io_push_fire) begin
      colInPlusDelta_b = {1'd0, _zz_colInPlusDelta_b};
    end else begin
      colInPlusDelta_b = 7'h00;
    end
  end

  assign when_lcvVideoDithererMod_l331 = colInPlusDelta_r[6];
  always @(*) begin
    if(io_push_fire) begin
      if(when_lcvVideoDithererMod_l331) begin
        dicol_r = 6'h3f;
      end else begin
        dicol_r = colInPlusDelta_r[5 : 0];
      end
    end else begin
      dicol_r = 6'h00;
    end
  end

  assign when_lcvVideoDithererMod_l345 = colInPlusDelta_g[6];
  always @(*) begin
    if(io_push_fire) begin
      if(when_lcvVideoDithererMod_l345) begin
        dicol_g = 6'h3f;
      end else begin
        dicol_g = colInPlusDelta_g[5 : 0];
      end
    end else begin
      dicol_g = 6'h00;
    end
  end

  assign when_lcvVideoDithererMod_l359 = colInPlusDelta_b[6];
  always @(*) begin
    if(io_push_fire) begin
      if(when_lcvVideoDithererMod_l359) begin
        dicol_b = 6'h3f;
      end else begin
        dicol_b = colInPlusDelta_b[5 : 0];
      end
    end else begin
      dicol_b = 6'h00;
    end
  end

  always @(*) begin
    if(io_push_fire) begin
      fifo_io_push_payload_col_r = dicol_r[5 : 2];
    end else begin
      fifo_io_push_payload_col_r = rPastTempPayload_col_r;
    end
  end

  always @(*) begin
    if(io_push_fire) begin
      fifo_io_push_payload_col_g = dicol_g[5 : 2];
    end else begin
      fifo_io_push_payload_col_g = rPastTempPayload_col_g;
    end
  end

  always @(*) begin
    if(io_push_fire) begin
      fifo_io_push_payload_col_b = dicol_b[5 : 2];
    end else begin
      fifo_io_push_payload_col_b = rPastTempPayload_col_b;
    end
  end

  always @(posedge clk or posedge reset) begin
    if(reset) begin
      rPastTempPayload_frameCnt <= 2'b00;
      rPastTempPayload_pos_x <= 16'h0000;
      rPastTempPayload_pos_y <= 16'h0000;
      rPastTempPayload_pastPos_x <= 16'h0000;
      rPastTempPayload_pastPos_y <= 16'h0000;
      rPastTempPayload_col_r <= 4'b0000;
      rPastTempPayload_col_g <= 4'b0000;
      rPastTempPayload_col_b <= 4'b0000;
      rPastTempPayloadPosY <= 16'h0000;
    end else begin
      if(io_push_fire) begin
        rPastTempPayload_frameCnt <= fifo_io_push_payload_frameCnt;
        rPastTempPayload_pos_x <= io_outpNextPos_x;
        rPastTempPayload_pos_y <= io_outpNextPos_y;
        rPastTempPayload_pastPos_x <= rPastTempPayload_pos_x;
        rPastTempPayload_pastPos_y <= rPastTempPayload_pos_y;
        rPastTempPayload_col_r <= fifo_io_push_payload_col_r;
        rPastTempPayload_col_g <= fifo_io_push_payload_col_g;
        rPastTempPayload_col_b <= fifo_io_push_payload_col_b;
      end
      rPastTempPayloadPosY <= io_outpNextPos_y;
    end
  end


endmodule

module LcvVgaCtrl (
  input               io_en,
  input               io_push_valid,
  output              io_push_ready,
  input      [3:0]    io_push_payload_r,
  input      [3:0]    io_push_payload_g,
  input      [3:0]    io_push_payload_b,
  output     [3:0]    io_phys_col_r,
  output     [3:0]    io_phys_col_g,
  output     [3:0]    io_phys_col_b,
  output              io_phys_hsync,
  output              io_phys_vsync,
  output     [1:0]    io_misc_dbgHscS,
  output     [9:0]    io_misc_dbgHscC,
  output     [1:0]    io_misc_dbgHscNextS,
  output     [1:0]    io_misc_dbgVscS,
  output     [8:0]    io_misc_dbgVscC,
  output     [1:0]    io_misc_dbgVscNextS,
  output              io_misc_dbgFifoEmpty,
  output              io_misc_dbgFifoFull,
  output              io_misc_pixelEn,
  output              io_misc_nextVisib,
  output              io_misc_visib,
  output              io_misc_pastVisib,
  output     [15:0]   io_misc_drawPos_x,
  output     [15:0]   io_misc_drawPos_y,
  output     [15:0]   io_misc_pastDrawPos_x,
  output     [15:0]   io_misc_pastDrawPos_y,
  output     [15:0]   io_misc_size_x,
  output     [15:0]   io_misc_size_y,
  input               reset,
  input               clk
);
  localparam LcvVgaState_front = 2'd0;
  localparam LcvVgaState_sync = 2'd1;
  localparam LcvVgaState_back = 2'd2;
  localparam LcvVgaState_visib = 2'd3;

  wire                fifo_io_pop_ready;
  wire                fifo_io_push_ready;
  wire                fifo_io_pop_valid;
  wire       [3:0]    fifo_io_pop_payload_r;
  wire       [3:0]    fifo_io_pop_payload_g;
  wire       [3:0]    fifo_io_pop_payload_b;
  wire                fifo_io_misc_empty;
  wire                fifo_io_misc_full;
  wire       [2:0]    _zz_clkCntP1;
  wire       [10:0]   _zz__zz_when_lcvVgaCtrlMod_l128;
  wire       [10:0]   _zz__zz_when_lcvVgaCtrlMod_l136;
  wire       [9:0]    _zz__zz_when_lcvVgaCtrlMod_l128_1;
  wire       [9:0]    _zz__zz_when_lcvVgaCtrlMod_l136_1;
  wire       [9:0]    _zz_when_lcvVgaCtrlMod_l628;
  wire       [8:0]    _zz_when_lcvVgaCtrlMod_l628_1;
  reg        [3:0]    rPhys_col_r;
  reg        [3:0]    rPhys_col_g;
  reg        [3:0]    rPhys_col_b;
  reg                 rPhys_hsync;
  reg                 rPhys_vsync;
  reg        [1:0]    clkCnt;
  reg        [1:0]    clkCntNext;
  wire       [2:0]    clkCntP1;
  wire                when_lcvVgaCtrlMod_l340;
  wire                nextPixelEn;
  wire                pixelEnNextCycle;
  reg        [1:0]    switch_lcvVgaCtrlMod_l144;
  reg        [9:0]    _zz_io_misc_dbgHscC;
  wire       [10:0]   _zz_when_lcvVgaCtrlMod_l128;
  wire       [10:0]   _zz_when_lcvVgaCtrlMod_l136;
  reg        [1:0]    _zz_rNextVisib;
  reg        [1:0]    switch_lcvVgaCtrlMod_l144_1;
  reg        [8:0]    _zz_io_misc_dbgVscC;
  wire       [9:0]    _zz_when_lcvVgaCtrlMod_l128_1;
  wire       [9:0]    _zz_when_lcvVgaCtrlMod_l136_1;
  reg        [1:0]    _zz_io_misc_dbgHscNextS;
  wire                when_lcvVgaCtrlMod_l128;
  wire                when_lcvVgaCtrlMod_l136;
  wire                when_lcvVgaCtrlMod_l128_1;
  wire                when_lcvVgaCtrlMod_l136_1;
  wire                when_lcvVgaCtrlMod_l128_2;
  wire                when_lcvVgaCtrlMod_l136_2;
  wire                when_lcvVgaCtrlMod_l128_3;
  wire                when_lcvVgaCtrlMod_l136_3;
  wire                when_lcvVgaCtrlMod_l408;
  wire                when_lcvVgaCtrlMod_l128_4;
  wire                when_lcvVgaCtrlMod_l136_4;
  wire                when_lcvVgaCtrlMod_l128_5;
  wire                when_lcvVgaCtrlMod_l136_5;
  wire                when_lcvVgaCtrlMod_l128_6;
  wire                when_lcvVgaCtrlMod_l136_6;
  wire                when_lcvVgaCtrlMod_l128_7;
  wire                when_lcvVgaCtrlMod_l136_7;
  wire                when_lcvVgaCtrlMod_l484;
  reg                 rPastFifoPopReady;
  reg                 rNextVisib;
  reg                 rVisib;
  reg                 rPastVisib;
  reg        [15:0]   rPastDrawPos_x;
  reg        [15:0]   rPastDrawPos_y;
  (* keep *) wire                hscNextSVisib;
  (* keep *) wire                vscNextSVisib;
  reg                 formal_with_past_after_reset;
  reg                 io_misc_pixelEn_past_1;
  wire                when_lcvVgaCtrlMod_l628;
  `ifndef SYNTHESIS
  reg [39:0] io_misc_dbgHscS_string;
  reg [39:0] io_misc_dbgHscNextS_string;
  reg [39:0] io_misc_dbgVscS_string;
  reg [39:0] io_misc_dbgVscNextS_string;
  reg [39:0] switch_lcvVgaCtrlMod_l144_string;
  reg [39:0] _zz_rNextVisib_string;
  reg [39:0] switch_lcvVgaCtrlMod_l144_1_string;
  reg [39:0] _zz_io_misc_dbgHscNextS_string;
  `endif


  assign _zz_clkCntP1 = {1'd0, clkCnt};
  assign _zz__zz_when_lcvVgaCtrlMod_l128 = {1'd0, _zz_io_misc_dbgHscC};
  assign _zz__zz_when_lcvVgaCtrlMod_l136 = {1'd0, _zz_io_misc_dbgHscC};
  assign _zz__zz_when_lcvVgaCtrlMod_l128_1 = {1'd0, _zz_io_misc_dbgVscC};
  assign _zz__zz_when_lcvVgaCtrlMod_l136_1 = {1'd0, _zz_io_misc_dbgVscC};
  assign _zz_when_lcvVgaCtrlMod_l628 = io_misc_drawPos_x[9:0];
  assign _zz_when_lcvVgaCtrlMod_l628_1 = io_misc_drawPos_y[8:0];
  AsyncReadFifo_1 fifo (
    .io_push_valid     (io_push_valid             ), //i
    .io_push_ready     (fifo_io_push_ready        ), //o
    .io_push_payload_r (io_push_payload_r[3:0]    ), //i
    .io_push_payload_g (io_push_payload_g[3:0]    ), //i
    .io_push_payload_b (io_push_payload_b[3:0]    ), //i
    .io_pop_valid      (fifo_io_pop_valid         ), //o
    .io_pop_ready      (fifo_io_pop_ready         ), //i
    .io_pop_payload_r  (fifo_io_pop_payload_r[3:0]), //o
    .io_pop_payload_g  (fifo_io_pop_payload_g[3:0]), //o
    .io_pop_payload_b  (fifo_io_pop_payload_b[3:0]), //o
    .io_misc_empty     (fifo_io_misc_empty        ), //o
    .io_misc_full      (fifo_io_misc_full         ), //o
    .reset             (reset                     ), //i
    .clk               (clk                       )  //i
  );
  initial begin
    assume((((((io_phys_col_r == 4'b0000) && (io_phys_col_g == 4'b0000)) && (io_phys_col_b == 4'b0000)) && (io_phys_hsync == 1'b0)) && (io_phys_vsync == 1'b0))); // lcvVgaCtrlMod.scala:L326
    assume((((((rPhys_col_r == 4'b0000) && (rPhys_col_g == 4'b0000)) && (rPhys_col_b == 4'b0000)) && (rPhys_hsync == 1'b0)) && (rPhys_vsync == 1'b0))); // lcvVgaCtrlMod.scala:L327
  end

  `ifndef SYNTHESIS
  always @(*) begin
    case(io_misc_dbgHscS)
      LcvVgaState_front : io_misc_dbgHscS_string = "front";
      LcvVgaState_sync : io_misc_dbgHscS_string = "sync ";
      LcvVgaState_back : io_misc_dbgHscS_string = "back ";
      LcvVgaState_visib : io_misc_dbgHscS_string = "visib";
      default : io_misc_dbgHscS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_misc_dbgHscNextS)
      LcvVgaState_front : io_misc_dbgHscNextS_string = "front";
      LcvVgaState_sync : io_misc_dbgHscNextS_string = "sync ";
      LcvVgaState_back : io_misc_dbgHscNextS_string = "back ";
      LcvVgaState_visib : io_misc_dbgHscNextS_string = "visib";
      default : io_misc_dbgHscNextS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_misc_dbgVscS)
      LcvVgaState_front : io_misc_dbgVscS_string = "front";
      LcvVgaState_sync : io_misc_dbgVscS_string = "sync ";
      LcvVgaState_back : io_misc_dbgVscS_string = "back ";
      LcvVgaState_visib : io_misc_dbgVscS_string = "visib";
      default : io_misc_dbgVscS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_misc_dbgVscNextS)
      LcvVgaState_front : io_misc_dbgVscNextS_string = "front";
      LcvVgaState_sync : io_misc_dbgVscNextS_string = "sync ";
      LcvVgaState_back : io_misc_dbgVscNextS_string = "back ";
      LcvVgaState_visib : io_misc_dbgVscNextS_string = "visib";
      default : io_misc_dbgVscNextS_string = "?????";
    endcase
  end
  always @(*) begin
    case(switch_lcvVgaCtrlMod_l144)
      LcvVgaState_front : switch_lcvVgaCtrlMod_l144_string = "front";
      LcvVgaState_sync : switch_lcvVgaCtrlMod_l144_string = "sync ";
      LcvVgaState_back : switch_lcvVgaCtrlMod_l144_string = "back ";
      LcvVgaState_visib : switch_lcvVgaCtrlMod_l144_string = "visib";
      default : switch_lcvVgaCtrlMod_l144_string = "?????";
    endcase
  end
  always @(*) begin
    case(_zz_rNextVisib)
      LcvVgaState_front : _zz_rNextVisib_string = "front";
      LcvVgaState_sync : _zz_rNextVisib_string = "sync ";
      LcvVgaState_back : _zz_rNextVisib_string = "back ";
      LcvVgaState_visib : _zz_rNextVisib_string = "visib";
      default : _zz_rNextVisib_string = "?????";
    endcase
  end
  always @(*) begin
    case(switch_lcvVgaCtrlMod_l144_1)
      LcvVgaState_front : switch_lcvVgaCtrlMod_l144_1_string = "front";
      LcvVgaState_sync : switch_lcvVgaCtrlMod_l144_1_string = "sync ";
      LcvVgaState_back : switch_lcvVgaCtrlMod_l144_1_string = "back ";
      LcvVgaState_visib : switch_lcvVgaCtrlMod_l144_1_string = "visib";
      default : switch_lcvVgaCtrlMod_l144_1_string = "?????";
    endcase
  end
  always @(*) begin
    case(_zz_io_misc_dbgHscNextS)
      LcvVgaState_front : _zz_io_misc_dbgHscNextS_string = "front";
      LcvVgaState_sync : _zz_io_misc_dbgHscNextS_string = "sync ";
      LcvVgaState_back : _zz_io_misc_dbgHscNextS_string = "back ";
      LcvVgaState_visib : _zz_io_misc_dbgHscNextS_string = "visib";
      default : _zz_io_misc_dbgHscNextS_string = "?????";
    endcase
  end
  `endif

  assign io_phys_col_r = rPhys_col_r;
  assign io_phys_col_g = rPhys_col_g;
  assign io_phys_col_b = rPhys_col_b;
  assign io_phys_hsync = rPhys_hsync;
  assign io_phys_vsync = rPhys_vsync;
  always @(*) begin
    clkCntNext = clkCnt;
    if(when_lcvVgaCtrlMod_l340) begin
      clkCntNext = clkCntP1[1 : 0];
    end else begin
      clkCntNext = 2'b00;
    end
  end

  assign clkCntP1 = (_zz_clkCntP1 + 3'b001);
  assign when_lcvVgaCtrlMod_l340 = (clkCntP1 < 3'b100);
  assign io_misc_pixelEn = (clkCnt == 2'b00);
  assign nextPixelEn = (clkCntNext == 2'b00);
  assign pixelEnNextCycle = (clkCntP1 == 3'b100);
  assign _zz_when_lcvVgaCtrlMod_l128 = (_zz__zz_when_lcvVgaCtrlMod_l128 + 11'h001);
  assign _zz_when_lcvVgaCtrlMod_l136 = (_zz__zz_when_lcvVgaCtrlMod_l136 + 11'h002);
  assign _zz_when_lcvVgaCtrlMod_l128_1 = (_zz__zz_when_lcvVgaCtrlMod_l128_1 + 10'h001);
  assign _zz_when_lcvVgaCtrlMod_l136_1 = (_zz__zz_when_lcvVgaCtrlMod_l136_1 + 10'h002);
  assign io_misc_dbgHscS = switch_lcvVgaCtrlMod_l144;
  assign io_misc_dbgHscC = _zz_io_misc_dbgHscC;
  assign io_misc_dbgHscNextS = _zz_io_misc_dbgHscNextS;
  assign io_misc_dbgVscS = switch_lcvVgaCtrlMod_l144_1;
  assign io_misc_dbgVscC = _zz_io_misc_dbgVscC;
  assign io_misc_dbgVscNextS = _zz_io_misc_dbgHscNextS;
  assign when_lcvVgaCtrlMod_l128 = (11'h010 < _zz_when_lcvVgaCtrlMod_l128);
  assign when_lcvVgaCtrlMod_l136 = (11'h010 < _zz_when_lcvVgaCtrlMod_l136);
  always @(*) begin
    if(io_misc_pixelEn) begin
      case(switch_lcvVgaCtrlMod_l144)
        LcvVgaState_front : begin
          if(when_lcvVgaCtrlMod_l136) begin
            _zz_rNextVisib = LcvVgaState_sync;
          end else begin
            _zz_rNextVisib = switch_lcvVgaCtrlMod_l144;
          end
        end
        LcvVgaState_sync : begin
          if(when_lcvVgaCtrlMod_l136_1) begin
            _zz_rNextVisib = LcvVgaState_back;
          end else begin
            _zz_rNextVisib = switch_lcvVgaCtrlMod_l144;
          end
        end
        LcvVgaState_back : begin
          if(when_lcvVgaCtrlMod_l136_2) begin
            _zz_rNextVisib = LcvVgaState_visib;
          end else begin
            _zz_rNextVisib = switch_lcvVgaCtrlMod_l144;
          end
        end
        default : begin
          if(when_lcvVgaCtrlMod_l136_3) begin
            _zz_rNextVisib = LcvVgaState_front;
          end else begin
            _zz_rNextVisib = switch_lcvVgaCtrlMod_l144;
          end
        end
      endcase
    end else begin
      _zz_rNextVisib = switch_lcvVgaCtrlMod_l144;
    end
  end

  assign when_lcvVgaCtrlMod_l128_1 = (11'h060 < _zz_when_lcvVgaCtrlMod_l128);
  assign when_lcvVgaCtrlMod_l136_1 = (11'h060 < _zz_when_lcvVgaCtrlMod_l136);
  assign when_lcvVgaCtrlMod_l128_2 = (11'h030 < _zz_when_lcvVgaCtrlMod_l128);
  assign when_lcvVgaCtrlMod_l136_2 = (11'h030 < _zz_when_lcvVgaCtrlMod_l136);
  assign when_lcvVgaCtrlMod_l128_3 = (11'h280 < _zz_when_lcvVgaCtrlMod_l128);
  assign when_lcvVgaCtrlMod_l136_3 = (11'h280 < _zz_when_lcvVgaCtrlMod_l136);
  always @(*) begin
    if(io_misc_pixelEn) begin
      case(switch_lcvVgaCtrlMod_l144)
        LcvVgaState_front : begin
          _zz_io_misc_dbgHscNextS = switch_lcvVgaCtrlMod_l144_1;
        end
        LcvVgaState_sync : begin
          _zz_io_misc_dbgHscNextS = switch_lcvVgaCtrlMod_l144_1;
        end
        LcvVgaState_back : begin
          _zz_io_misc_dbgHscNextS = switch_lcvVgaCtrlMod_l144_1;
        end
        default : begin
          if(when_lcvVgaCtrlMod_l408) begin
            case(switch_lcvVgaCtrlMod_l144_1)
              LcvVgaState_front : begin
                if(when_lcvVgaCtrlMod_l136_4) begin
                  _zz_io_misc_dbgHscNextS = LcvVgaState_sync;
                end else begin
                  _zz_io_misc_dbgHscNextS = switch_lcvVgaCtrlMod_l144_1;
                end
              end
              LcvVgaState_sync : begin
                if(when_lcvVgaCtrlMod_l136_5) begin
                  _zz_io_misc_dbgHscNextS = LcvVgaState_back;
                end else begin
                  _zz_io_misc_dbgHscNextS = switch_lcvVgaCtrlMod_l144_1;
                end
              end
              LcvVgaState_back : begin
                if(when_lcvVgaCtrlMod_l136_6) begin
                  _zz_io_misc_dbgHscNextS = LcvVgaState_visib;
                end else begin
                  _zz_io_misc_dbgHscNextS = switch_lcvVgaCtrlMod_l144_1;
                end
              end
              default : begin
                if(when_lcvVgaCtrlMod_l136_7) begin
                  _zz_io_misc_dbgHscNextS = LcvVgaState_front;
                end else begin
                  _zz_io_misc_dbgHscNextS = switch_lcvVgaCtrlMod_l144_1;
                end
              end
            endcase
          end else begin
            _zz_io_misc_dbgHscNextS = switch_lcvVgaCtrlMod_l144_1;
          end
        end
      endcase
    end else begin
      _zz_io_misc_dbgHscNextS = switch_lcvVgaCtrlMod_l144_1;
    end
  end

  assign when_lcvVgaCtrlMod_l408 = (11'h280 <= _zz_when_lcvVgaCtrlMod_l128);
  assign when_lcvVgaCtrlMod_l128_4 = (10'h00a < _zz_when_lcvVgaCtrlMod_l128_1);
  assign when_lcvVgaCtrlMod_l136_4 = (10'h00a < _zz_when_lcvVgaCtrlMod_l136_1);
  assign when_lcvVgaCtrlMod_l128_5 = (10'h002 < _zz_when_lcvVgaCtrlMod_l128_1);
  assign when_lcvVgaCtrlMod_l136_5 = (10'h002 < _zz_when_lcvVgaCtrlMod_l136_1);
  assign when_lcvVgaCtrlMod_l128_6 = (10'h021 < _zz_when_lcvVgaCtrlMod_l128_1);
  assign when_lcvVgaCtrlMod_l136_6 = (10'h021 < _zz_when_lcvVgaCtrlMod_l136_1);
  assign when_lcvVgaCtrlMod_l128_7 = (10'h1e0 < _zz_when_lcvVgaCtrlMod_l128_1);
  assign when_lcvVgaCtrlMod_l136_7 = (10'h1e0 < _zz_when_lcvVgaCtrlMod_l136_1);
  assign when_lcvVgaCtrlMod_l484 = (! io_en);
  assign io_push_ready = fifo_io_push_ready;
  assign fifo_io_pop_ready = ((nextPixelEn && io_misc_nextVisib) && (! rPastFifoPopReady));
  assign io_misc_dbgFifoEmpty = fifo_io_misc_empty;
  assign io_misc_dbgFifoFull = fifo_io_misc_full;
  assign io_misc_drawPos_x = {6'd0, _zz_io_misc_dbgHscC};
  assign io_misc_drawPos_y = {7'd0, _zz_io_misc_dbgVscC};
  assign io_misc_size_x = 16'h0280;
  assign io_misc_size_y = 16'h01e0;
  assign io_misc_nextVisib = rNextVisib;
  assign io_misc_visib = rVisib;
  assign io_misc_pastVisib = rPastVisib;
  assign io_misc_pastDrawPos_x = rPastDrawPos_x;
  assign io_misc_pastDrawPos_y = rPastDrawPos_y;
  assign hscNextSVisib = (_zz_rNextVisib == LcvVgaState_visib);
  assign vscNextSVisib = (_zz_io_misc_dbgHscNextS == LcvVgaState_visib);
  assign when_lcvVgaCtrlMod_l628 = ((((io_misc_pixelEn_past_1 && hscNextSVisib) && vscNextSVisib) && (_zz_when_lcvVgaCtrlMod_l628 < 10'h280)) && (_zz_when_lcvVgaCtrlMod_l628_1 < 9'h1e0));
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      rPhys_col_r <= 4'b0000;
      rPhys_col_g <= 4'b0000;
      rPhys_col_b <= 4'b0000;
      rPhys_hsync <= 1'b0;
      rPhys_vsync <= 1'b0;
      clkCnt <= 2'b00;
      switch_lcvVgaCtrlMod_l144 <= LcvVgaState_front;
      _zz_io_misc_dbgHscC <= 10'h000;
      switch_lcvVgaCtrlMod_l144_1 <= LcvVgaState_front;
      _zz_io_misc_dbgVscC <= 9'h000;
      rPastFifoPopReady <= 1'b0;
      rNextVisib <= 1'b0;
      rVisib <= 1'b0;
      rPastVisib <= 1'b0;
      rPastDrawPos_x <= 16'h0000;
      rPastDrawPos_y <= 16'h0000;
      formal_with_past_after_reset <= 1'b0;
    end else begin
      clkCnt <= clkCntNext;
      if(io_misc_pixelEn) begin
        case(switch_lcvVgaCtrlMod_l144)
          LcvVgaState_front : begin
            if(when_lcvVgaCtrlMod_l128) begin
              switch_lcvVgaCtrlMod_l144 <= LcvVgaState_sync;
              _zz_io_misc_dbgHscC <= 10'h000;
            end else begin
              _zz_io_misc_dbgHscC <= _zz_when_lcvVgaCtrlMod_l128[9:0];
            end
          end
          LcvVgaState_sync : begin
            if(when_lcvVgaCtrlMod_l128_1) begin
              switch_lcvVgaCtrlMod_l144 <= LcvVgaState_back;
              _zz_io_misc_dbgHscC <= 10'h000;
            end else begin
              _zz_io_misc_dbgHscC <= _zz_when_lcvVgaCtrlMod_l128[9:0];
            end
          end
          LcvVgaState_back : begin
            if(when_lcvVgaCtrlMod_l128_2) begin
              switch_lcvVgaCtrlMod_l144 <= LcvVgaState_visib;
              _zz_io_misc_dbgHscC <= 10'h000;
            end else begin
              _zz_io_misc_dbgHscC <= _zz_when_lcvVgaCtrlMod_l128[9:0];
            end
          end
          default : begin
            if(when_lcvVgaCtrlMod_l128_3) begin
              switch_lcvVgaCtrlMod_l144 <= LcvVgaState_front;
              _zz_io_misc_dbgHscC <= 10'h000;
            end else begin
              _zz_io_misc_dbgHscC <= _zz_when_lcvVgaCtrlMod_l128[9:0];
            end
          end
        endcase
        case(switch_lcvVgaCtrlMod_l144)
          LcvVgaState_front : begin
            rPhys_hsync <= 1'b1;
          end
          LcvVgaState_sync : begin
            rPhys_hsync <= 1'b0;
          end
          LcvVgaState_back : begin
            rPhys_hsync <= 1'b1;
          end
          default : begin
            rPhys_hsync <= 1'b1;
            if(when_lcvVgaCtrlMod_l408) begin
              case(switch_lcvVgaCtrlMod_l144_1)
                LcvVgaState_front : begin
                  if(when_lcvVgaCtrlMod_l128_4) begin
                    switch_lcvVgaCtrlMod_l144_1 <= LcvVgaState_sync;
                    _zz_io_misc_dbgVscC <= 9'h000;
                  end else begin
                    _zz_io_misc_dbgVscC <= _zz_when_lcvVgaCtrlMod_l128_1[8:0];
                  end
                end
                LcvVgaState_sync : begin
                  if(when_lcvVgaCtrlMod_l128_5) begin
                    switch_lcvVgaCtrlMod_l144_1 <= LcvVgaState_back;
                    _zz_io_misc_dbgVscC <= 9'h000;
                  end else begin
                    _zz_io_misc_dbgVscC <= _zz_when_lcvVgaCtrlMod_l128_1[8:0];
                  end
                end
                LcvVgaState_back : begin
                  if(when_lcvVgaCtrlMod_l128_6) begin
                    switch_lcvVgaCtrlMod_l144_1 <= LcvVgaState_visib;
                    _zz_io_misc_dbgVscC <= 9'h000;
                  end else begin
                    _zz_io_misc_dbgVscC <= _zz_when_lcvVgaCtrlMod_l128_1[8:0];
                  end
                end
                default : begin
                  if(when_lcvVgaCtrlMod_l128_7) begin
                    switch_lcvVgaCtrlMod_l144_1 <= LcvVgaState_front;
                    _zz_io_misc_dbgVscC <= 9'h000;
                  end else begin
                    _zz_io_misc_dbgVscC <= _zz_when_lcvVgaCtrlMod_l128_1[8:0];
                  end
                end
              endcase
            end
          end
        endcase
        case(switch_lcvVgaCtrlMod_l144_1)
          LcvVgaState_front : begin
            rPhys_vsync <= 1'b1;
          end
          LcvVgaState_sync : begin
            rPhys_vsync <= 1'b0;
          end
          LcvVgaState_back : begin
            rPhys_vsync <= 1'b1;
          end
          default : begin
            rPhys_vsync <= 1'b1;
          end
        endcase
      end
      if(io_misc_pixelEn) begin
        if(io_misc_visib) begin
          if(when_lcvVgaCtrlMod_l484) begin
            rPhys_col_r <= 4'b1111;
            rPhys_col_g <= 4'b1111;
            rPhys_col_b <= 4'b1111;
          end else begin
            rPhys_col_r <= fifo_io_pop_payload_r;
            rPhys_col_g <= fifo_io_pop_payload_g;
            rPhys_col_b <= fifo_io_pop_payload_b;
          end
        end else begin
          rPhys_col_r <= 4'b0000;
          rPhys_col_g <= 4'b0000;
          rPhys_col_b <= 4'b0000;
        end
      end else begin
        rPhys_col_r <= 4'b0000;
        rPhys_col_g <= 4'b0000;
        rPhys_col_b <= 4'b0000;
        rPhys_hsync <= 1'b0;
        rPhys_vsync <= 1'b0;
      end
      rPastFifoPopReady <= fifo_io_pop_ready;
      rNextVisib <= ((_zz_rNextVisib == LcvVgaState_visib) && (_zz_io_misc_dbgHscNextS == LcvVgaState_visib));
      rVisib <= io_misc_nextVisib;
      rPastVisib <= io_misc_visib;
      rPastDrawPos_x <= io_misc_drawPos_x;
      rPastDrawPos_y <= io_misc_drawPos_y;
      formal_with_past_after_reset <= (! reset);
      if(formal_with_past_after_reset) begin
        if(when_lcvVgaCtrlMod_l628) begin
          assert(io_misc_nextVisib); // lcvVgaCtrlMod.scala:L629
        end
      end
    end
  end

  always @(posedge clk) begin
    io_misc_pixelEn_past_1 <= io_misc_pixelEn;
  end


endmodule

module PipeSkidBuf (
  (* keep *) output              io_next_valid,
  (* keep *) input               io_next_ready,
  (* keep *) output reg [5:0]    io_next_payload_r,
  (* keep *) output reg [5:0]    io_next_payload_g,
  (* keep *) output reg [5:0]    io_next_payload_b,
  (* keep , keep *) input               io_prev_valid,
  (* keep , keep *) output              io_prev_ready,
  (* keep , keep *) input      [5:0]    io_prev_payload_r,
  (* keep , keep *) input      [5:0]    io_prev_payload_g,
  (* keep , keep *) input      [5:0]    io_prev_payload_b,
  (* keep *) input               io_misc_clear,
  input               reset,
  input               clk
);

  (* keep *) wire                _zz_io_next_valid;
  (* keep *) wire                _zz_io_prev_s2mPipe_m2sPipe_ready;
  (* keep *) wire       [5:0]    _zz_io_next_payload_r;
  (* keep *) wire       [5:0]    _zz_io_next_payload_g;
  (* keep *) wire       [5:0]    _zz_io_next_payload_b;
  wire                io_prev_s2mPipe_valid;
  reg                 io_prev_s2mPipe_ready;
  wire       [5:0]    io_prev_s2mPipe_payload_r;
  wire       [5:0]    io_prev_s2mPipe_payload_g;
  wire       [5:0]    io_prev_s2mPipe_payload_b;
  reg                 io_prev_rValidN;
  reg        [5:0]    io_prev_rData_r;
  reg        [5:0]    io_prev_rData_g;
  reg        [5:0]    io_prev_rData_b;
  wire                io_prev_s2mPipe_m2sPipe_valid;
  wire                io_prev_s2mPipe_m2sPipe_ready;
  wire       [5:0]    io_prev_s2mPipe_m2sPipe_payload_r;
  wire       [5:0]    io_prev_s2mPipe_m2sPipe_payload_g;
  wire       [5:0]    io_prev_s2mPipe_m2sPipe_payload_b;
  reg                 io_prev_s2mPipe_rValid;
  reg        [5:0]    io_prev_s2mPipe_rData_r;
  reg        [5:0]    io_prev_s2mPipe_rData_g;
  reg        [5:0]    io_prev_s2mPipe_rData_b;
  wire                when_Stream_l369;
  wire                io_prev_fire;
  reg                 formal_with_past_after_reset;
  reg                 _zz_1;
  reg        [5:0]    _zz_2;
  reg        [5:0]    _zz_3;
  reg        [5:0]    _zz_4;
  wire                io_next_isStall;
  reg                 io_next_isStall_past_1;
  reg        [5:0]    io_next_payload_regNext_r;
  reg        [5:0]    io_next_payload_regNext_g;
  reg        [5:0]    io_next_payload_regNext_b;

  assign io_prev_ready = io_prev_rValidN;
  assign io_prev_s2mPipe_valid = (io_prev_valid || (! io_prev_rValidN));
  assign io_prev_s2mPipe_payload_r = (io_prev_rValidN ? io_prev_payload_r : io_prev_rData_r);
  assign io_prev_s2mPipe_payload_g = (io_prev_rValidN ? io_prev_payload_g : io_prev_rData_g);
  assign io_prev_s2mPipe_payload_b = (io_prev_rValidN ? io_prev_payload_b : io_prev_rData_b);
  always @(*) begin
    io_prev_s2mPipe_ready = io_prev_s2mPipe_m2sPipe_ready;
    if(when_Stream_l369) begin
      io_prev_s2mPipe_ready = 1'b1;
    end
  end

  assign when_Stream_l369 = (! io_prev_s2mPipe_m2sPipe_valid);
  assign io_prev_s2mPipe_m2sPipe_valid = io_prev_s2mPipe_rValid;
  assign io_prev_s2mPipe_m2sPipe_payload_r = io_prev_s2mPipe_rData_r;
  assign io_prev_s2mPipe_m2sPipe_payload_g = io_prev_s2mPipe_rData_g;
  assign io_prev_s2mPipe_m2sPipe_payload_b = io_prev_s2mPipe_rData_b;
  assign _zz_io_next_valid = io_prev_s2mPipe_m2sPipe_valid;
  assign io_prev_s2mPipe_m2sPipe_ready = _zz_io_prev_s2mPipe_m2sPipe_ready;
  assign _zz_io_next_payload_r = io_prev_s2mPipe_m2sPipe_payload_r;
  assign _zz_io_next_payload_g = io_prev_s2mPipe_m2sPipe_payload_g;
  assign _zz_io_next_payload_b = io_prev_s2mPipe_m2sPipe_payload_b;
  assign io_next_valid = _zz_io_next_valid;
  assign _zz_io_prev_s2mPipe_m2sPipe_ready = io_next_ready;
  always @(*) begin
    if(reset) begin
      io_next_payload_r = 6'h00;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_r = _zz_io_next_payload_r;
      end else begin
        io_next_payload_r = 6'h00;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      io_next_payload_g = 6'h00;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_g = _zz_io_next_payload_g;
      end else begin
        io_next_payload_g = 6'h00;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      io_next_payload_b = 6'h00;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_b = _zz_io_next_payload_b;
      end else begin
        io_next_payload_b = 6'h00;
      end
    end
  end

  assign io_prev_fire = (io_prev_valid && io_prev_ready);
  assign io_next_isStall = (io_next_valid && (! io_next_ready));
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      io_prev_rValidN <= 1'b1;
      io_prev_s2mPipe_rValid <= 1'b0;
      formal_with_past_after_reset <= 1'b0;
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
      formal_with_past_after_reset <= (! reset);
      if(formal_with_past_after_reset) begin
        if(_zz_1) begin
          assert(_zz_io_next_valid); // pipelineMods.scala:L253
          assert((((_zz_2 == _zz_io_next_payload_r) && (_zz_3 == _zz_io_next_payload_g)) && (_zz_4 == _zz_io_next_payload_b))); // pipelineMods.scala:L253
        end
        if(io_next_isStall_past_1) begin
          assume(io_next_valid); // pipelineMods.scala:L255
          assume((((io_next_payload_regNext_r == io_next_payload_r) && (io_next_payload_regNext_g == io_next_payload_g)) && (io_next_payload_regNext_b == io_next_payload_b))); // pipelineMods.scala:L255
        end
      end
    end
  end

  always @(posedge clk) begin
    if(io_prev_ready) begin
      io_prev_rData_r <= io_prev_payload_r;
      io_prev_rData_g <= io_prev_payload_g;
      io_prev_rData_b <= io_prev_payload_b;
    end
    if(io_prev_s2mPipe_ready) begin
      io_prev_s2mPipe_rData_r <= io_prev_s2mPipe_payload_r;
      io_prev_s2mPipe_rData_g <= io_prev_s2mPipe_payload_g;
      io_prev_s2mPipe_rData_b <= io_prev_s2mPipe_payload_b;
    end
  end

  always @(posedge clk or posedge reset) begin
    if(reset) begin
      _zz_1 <= 1'b0;
      io_next_isStall_past_1 <= 1'b0;
    end else begin
      _zz_1 <= (_zz_io_next_valid && (! _zz_io_prev_s2mPipe_m2sPipe_ready));
      io_next_isStall_past_1 <= io_next_isStall;
    end
  end

  always @(posedge clk) begin
    _zz_2 <= _zz_io_next_payload_r;
    _zz_3 <= _zz_io_next_payload_g;
    _zz_4 <= _zz_io_next_payload_b;
  end

  always @(posedge clk) begin
    io_next_payload_regNext_r <= io_next_payload_r;
    io_next_payload_regNext_g <= io_next_payload_g;
    io_next_payload_regNext_b <= io_next_payload_b;
  end


endmodule

module AsyncReadFifo (
  input               io_push_valid,
  output              io_push_ready,
  input      [1:0]    io_push_payload_frameCnt,
  input      [15:0]   io_push_payload_pos_x,
  input      [15:0]   io_push_payload_pos_y,
  input      [15:0]   io_push_payload_pastPos_x,
  input      [15:0]   io_push_payload_pastPos_y,
  input      [3:0]    io_push_payload_col_r,
  input      [3:0]    io_push_payload_col_g,
  input      [3:0]    io_push_payload_col_b,
  output              io_pop_valid,
  input               io_pop_ready,
  output     [1:0]    io_pop_payload_frameCnt,
  output     [15:0]   io_pop_payload_pos_x,
  output     [15:0]   io_pop_payload_pos_y,
  output     [15:0]   io_pop_payload_pastPos_x,
  output     [15:0]   io_pop_payload_pastPos_y,
  output     [3:0]    io_pop_payload_col_r,
  output     [3:0]    io_pop_payload_col_g,
  output     [3:0]    io_pop_payload_col_b,
  output              io_misc_empty,
  output              io_misc_full,
  input               reset,
  input               clk
);

  wire                sbPush_io_misc_clear;
  wire       [1:0]    sbPop_io_prev_payload_frameCnt;
  wire       [15:0]   sbPop_io_prev_payload_pos_x;
  wire       [15:0]   sbPop_io_prev_payload_pos_y;
  wire       [15:0]   sbPop_io_prev_payload_pastPos_x;
  wire       [15:0]   sbPop_io_prev_payload_pastPos_y;
  wire       [3:0]    sbPop_io_prev_payload_col_r;
  wire       [3:0]    sbPop_io_prev_payload_col_g;
  wire       [3:0]    sbPop_io_prev_payload_col_b;
  wire                sbPop_io_misc_clear;
  wire       [77:0]   _zz_loc_arr_port0;
  wire       [77:0]   _zz_loc_arr_port1;
  wire       [77:0]   _zz_loc_arr_port3;
  wire                sbPush_io_next_valid;
  wire       [1:0]    sbPush_io_next_payload_frameCnt;
  wire       [15:0]   sbPush_io_next_payload_pos_x;
  wire       [15:0]   sbPush_io_next_payload_pos_y;
  wire       [15:0]   sbPush_io_next_payload_pastPos_x;
  wire       [15:0]   sbPush_io_next_payload_pastPos_y;
  wire       [3:0]    sbPush_io_next_payload_col_r;
  wire       [3:0]    sbPush_io_next_payload_col_g;
  wire       [3:0]    sbPush_io_next_payload_col_b;
  wire                sbPush_io_prev_ready;
  wire                sbPop_io_next_valid;
  wire       [1:0]    sbPop_io_next_payload_frameCnt;
  wire       [15:0]   sbPop_io_next_payload_pos_x;
  wire       [15:0]   sbPop_io_next_payload_pos_y;
  wire       [15:0]   sbPop_io_next_payload_pastPos_x;
  wire       [15:0]   sbPop_io_next_payload_pastPos_y;
  wire       [3:0]    sbPop_io_next_payload_col_r;
  wire       [3:0]    sbPop_io_next_payload_col_g;
  wire       [3:0]    sbPop_io_next_payload_col_b;
  wire                sbPop_io_prev_ready;
  wire       [2:0]    _zz_loc_tailPlus1;
  wire       [2:0]    _zz_loc_headPlus1;
  wire       [2:0]    _zz_loc_nextHeadPlus1;
  wire       [2:0]    _zz_loc_nextEmpty;
  wire       [2:0]    _zz_loc_nextFull;
  wire       [1:0]    _zz_locFormal_testHead;
  wire       [2:0]    _zz_10;
  wire       [2:0]    _zz_11;
  wire       [1:0]    _zz_12;
  wire       [1:0]    _zz_13;
  wire       [1:0]    _zz_14;
  wire       [1:0]    _zz_15;
  wire       [77:0]   _zz_loc_arr_port;
  reg                 _zz_1;
  wire                wrEn;
  wire                rdValid;
  wire       [1:0]    rdDataPrev_frameCnt;
  wire       [15:0]   rdDataPrev_pos_x;
  wire       [15:0]   rdDataPrev_pos_y;
  wire       [15:0]   rdDataPrev_pastPos_x;
  wire       [15:0]   rdDataPrev_pastPos_y;
  wire       [3:0]    rdDataPrev_col_r;
  wire       [3:0]    rdDataPrev_col_g;
  wire       [3:0]    rdDataPrev_col_b;
  (* keep *) wire                rdEn;
  wire                fifo_sbPop_io_next_fire;
  reg                 loc_empty;
  reg                 loc_full;
  wire       [2:0]    loc_uintDepth;
  reg        [1:0]    loc_head;
  reg        [1:0]    loc_tail;
  reg        [1:0]    loc_nextHead;
  reg        [1:0]    loc_nextTail;
  (* keep *) wire       [2:0]    loc_tempTailPlus1;
  wire       [2:0]    loc_tailPlus1;
  (* keep *) wire       [2:0]    loc_tempHeadPlus1;
  wire       [2:0]    loc_headPlus1;
  (* keep *) wire       [2:0]    loc_tempNextHeadPlus1;
  wire       [2:0]    loc_nextHeadPlus1;
  (* keep *) wire                loc_tempOorNextHeadPlus1;
  wire                loc_oorNextHeadPlus1;
  (* keep *) reg        [2:0]    loc_tempIncrNextHead;
  wire                loc_nextEmpty;
  wire                loc_nextFull;
  (* keep *) reg        [1:0]    locFormal_lastTailVal_frameCnt;
  (* keep *) reg        [15:0]   locFormal_lastTailVal_pos_x;
  (* keep *) reg        [15:0]   locFormal_lastTailVal_pos_y;
  (* keep *) reg        [15:0]   locFormal_lastTailVal_pastPos_x;
  (* keep *) reg        [15:0]   locFormal_lastTailVal_pastPos_y;
  (* keep *) reg        [3:0]    locFormal_lastTailVal_col_r;
  (* keep *) reg        [3:0]    locFormal_lastTailVal_col_g;
  (* keep *) reg        [3:0]    locFormal_lastTailVal_col_b;
  wire       [1:0]    locFormal_testHead;
  wire       [77:0]   _zz_locFormal_lastTailVal_frameCnt;
  wire       [31:0]   _zz_locFormal_lastTailVal_pos_x;
  wire       [31:0]   _zz_locFormal_lastTailVal_pastPos_x;
  wire       [11:0]   _zz_locFormal_lastTailVal_col_r;
  wire       [77:0]   _zz_io_prev_payload_frameCnt;
  wire       [31:0]   _zz_io_prev_payload_pos_x;
  wire       [31:0]   _zz_io_prev_payload_pastPos_x;
  wire       [11:0]   _zz_io_prev_payload_col_r;
  wire                when_fifoMods_l640;
  wire                when_fifoMods_l643;
  wire                when_fifoMods_l657;
  wire                when_fifoMods_l658;
  wire                when_fifoMods_l734;
  wire                when_fifoMods_l738;
  reg                 formal_with_past_after_reset;
  wire       [77:0]   _zz_4;
  wire       [31:0]   _zz_5;
  wire       [31:0]   _zz_6;
  wire       [11:0]   _zz_7;
  reg                 rdEn_past_1;
  reg                 io_misc_empty_past_1;
  reg        [1:0]    loc_tail_regNext;
  reg        [1:0]    loc_tail_past_1;
  reg        [1:0]    loc_tail_regNext_1;
  reg                 wrEn_past_1;
  reg                 io_misc_full_past_1;
  reg        [1:0]    loc_head_regNext;
  reg        [1:0]    loc_head_past_1;
  reg        [1:0]    loc_head_regNext_1;
  wire       [1:0]    switch_fifoMods_l834;
  wire                when_fifoMods_l857;
  wire                when_fifoMods_l862;
  (* ram_style = "ultra" , keep *) reg [77:0] loc_arr [0:3];

  assign _zz_loc_tailPlus1 = {1'd0, loc_tail};
  assign _zz_loc_headPlus1 = {1'd0, loc_head};
  assign _zz_loc_nextHeadPlus1 = {1'd0, loc_nextHead};
  assign _zz_loc_nextEmpty = {1'd0, loc_nextTail};
  assign _zz_loc_nextFull = {1'd0, loc_nextTail};
  assign _zz_locFormal_testHead = (loc_head + 2'b01);
  assign _zz_10 = {1'd0, loc_tail};
  assign _zz_11 = {1'd0, loc_head};
  assign _zz_12 = (_zz_13 % 3'b100);
  assign _zz_13 = (loc_tail_past_1 + 2'b01);
  assign _zz_14 = (_zz_15 % 3'b100);
  assign _zz_15 = (loc_head_past_1 + 2'b01);
  assign _zz_loc_arr_port = {{io_push_payload_col_b,{io_push_payload_col_g,io_push_payload_col_r}},{{io_push_payload_pastPos_y,io_push_payload_pastPos_x},{{io_push_payload_pos_y,io_push_payload_pos_x},io_push_payload_frameCnt}}};
  assign _zz_loc_arr_port0 = loc_arr[loc_tail];
  assign _zz_loc_arr_port1 = loc_arr[loc_tail];
  always @(posedge clk) begin
    if(_zz_1) begin
      loc_arr[loc_head] <= _zz_loc_arr_port;
    end
  end

  assign _zz_loc_arr_port3 = loc_arr[loc_tail];
  PipeSkidBuf_1 sbPush (
    .io_next_valid             (sbPush_io_next_valid                  ), //o
    .io_next_ready             (1'b1                                  ), //i
    .io_next_payload_frameCnt  (sbPush_io_next_payload_frameCnt[1:0]  ), //o
    .io_next_payload_pos_x     (sbPush_io_next_payload_pos_x[15:0]    ), //o
    .io_next_payload_pos_y     (sbPush_io_next_payload_pos_y[15:0]    ), //o
    .io_next_payload_pastPos_x (sbPush_io_next_payload_pastPos_x[15:0]), //o
    .io_next_payload_pastPos_y (sbPush_io_next_payload_pastPos_y[15:0]), //o
    .io_next_payload_col_r     (sbPush_io_next_payload_col_r[3:0]     ), //o
    .io_next_payload_col_g     (sbPush_io_next_payload_col_g[3:0]     ), //o
    .io_next_payload_col_b     (sbPush_io_next_payload_col_b[3:0]     ), //o
    .io_prev_valid             (io_push_valid                         ), //i
    .io_prev_ready             (sbPush_io_prev_ready                  ), //o
    .io_prev_payload_frameCnt  (io_push_payload_frameCnt[1:0]         ), //i
    .io_prev_payload_pos_x     (io_push_payload_pos_x[15:0]           ), //i
    .io_prev_payload_pos_y     (io_push_payload_pos_y[15:0]           ), //i
    .io_prev_payload_pastPos_x (io_push_payload_pastPos_x[15:0]       ), //i
    .io_prev_payload_pastPos_y (io_push_payload_pastPos_y[15:0]       ), //i
    .io_prev_payload_col_r     (io_push_payload_col_r[3:0]            ), //i
    .io_prev_payload_col_g     (io_push_payload_col_g[3:0]            ), //i
    .io_prev_payload_col_b     (io_push_payload_col_b[3:0]            ), //i
    .io_misc_busy              (io_misc_full                          ), //i
    .io_misc_clear             (sbPush_io_misc_clear                  ), //i
    .reset                     (reset                                 ), //i
    .clk                       (clk                                   )  //i
  );
  PipeSkidBuf_1 sbPop (
    .io_next_valid             (sbPop_io_next_valid                  ), //o
    .io_next_ready             (io_pop_ready                         ), //i
    .io_next_payload_frameCnt  (sbPop_io_next_payload_frameCnt[1:0]  ), //o
    .io_next_payload_pos_x     (sbPop_io_next_payload_pos_x[15:0]    ), //o
    .io_next_payload_pos_y     (sbPop_io_next_payload_pos_y[15:0]    ), //o
    .io_next_payload_pastPos_x (sbPop_io_next_payload_pastPos_x[15:0]), //o
    .io_next_payload_pastPos_y (sbPop_io_next_payload_pastPos_y[15:0]), //o
    .io_next_payload_col_r     (sbPop_io_next_payload_col_r[3:0]     ), //o
    .io_next_payload_col_g     (sbPop_io_next_payload_col_g[3:0]     ), //o
    .io_next_payload_col_b     (sbPop_io_next_payload_col_b[3:0]     ), //o
    .io_prev_valid             (rdValid                              ), //i
    .io_prev_ready             (sbPop_io_prev_ready                  ), //o
    .io_prev_payload_frameCnt  (sbPop_io_prev_payload_frameCnt[1:0]  ), //i
    .io_prev_payload_pos_x     (sbPop_io_prev_payload_pos_x[15:0]    ), //i
    .io_prev_payload_pos_y     (sbPop_io_prev_payload_pos_y[15:0]    ), //i
    .io_prev_payload_pastPos_x (sbPop_io_prev_payload_pastPos_x[15:0]), //i
    .io_prev_payload_pastPos_y (sbPop_io_prev_payload_pastPos_y[15:0]), //i
    .io_prev_payload_col_r     (sbPop_io_prev_payload_col_r[3:0]     ), //i
    .io_prev_payload_col_g     (sbPop_io_prev_payload_col_g[3:0]     ), //i
    .io_prev_payload_col_b     (sbPop_io_prev_payload_col_b[3:0]     ), //i
    .io_misc_busy              (io_misc_empty                        ), //i
    .io_misc_clear             (sbPop_io_misc_clear                  ), //i
    .reset                     (reset                                ), //i
    .clk                       (clk                                  )  //i
  );
  always @(*) begin
    _zz_1 = 1'b0;
    if(when_fifoMods_l734) begin
      if(when_fifoMods_l738) begin
        _zz_1 = 1'b1;
      end
    end
  end

  assign io_push_ready = sbPush_io_prev_ready;
  assign wrEn = (io_push_valid && sbPush_io_prev_ready);
  assign io_pop_valid = sbPop_io_next_valid;
  assign io_pop_payload_frameCnt = sbPop_io_next_payload_frameCnt;
  assign io_pop_payload_pos_x = sbPop_io_next_payload_pos_x;
  assign io_pop_payload_pos_y = sbPop_io_next_payload_pos_y;
  assign io_pop_payload_pastPos_x = sbPop_io_next_payload_pastPos_x;
  assign io_pop_payload_pastPos_y = sbPop_io_next_payload_pastPos_y;
  assign io_pop_payload_col_r = sbPop_io_next_payload_col_r;
  assign io_pop_payload_col_g = sbPop_io_next_payload_col_g;
  assign io_pop_payload_col_b = sbPop_io_next_payload_col_b;
  assign rdDataPrev_frameCnt = 2'b00;
  assign rdDataPrev_pos_x = 16'h0000;
  assign rdDataPrev_pos_y = 16'h0000;
  assign rdDataPrev_pastPos_x = 16'h0000;
  assign rdDataPrev_pastPos_y = 16'h0000;
  assign rdDataPrev_col_r = 4'b0000;
  assign rdDataPrev_col_g = 4'b0000;
  assign rdDataPrev_col_b = 4'b0000;
  assign fifo_sbPop_io_next_fire = (sbPop_io_next_valid && io_pop_ready);
  assign rdEn = fifo_sbPop_io_next_fire;
  assign loc_uintDepth = 3'b100;
  assign loc_tailPlus1 = (_zz_loc_tailPlus1 + 3'b001);
  assign loc_tempTailPlus1 = loc_tailPlus1;
  assign loc_headPlus1 = (_zz_loc_headPlus1 + 3'b001);
  assign loc_tempHeadPlus1 = loc_headPlus1;
  assign loc_nextHeadPlus1 = (_zz_loc_nextHeadPlus1 + 3'b001);
  assign loc_tempNextHeadPlus1 = loc_nextHeadPlus1;
  assign loc_oorNextHeadPlus1 = (loc_uintDepth <= loc_nextHeadPlus1);
  assign loc_tempOorNextHeadPlus1 = loc_oorNextHeadPlus1;
  always @(*) begin
    if(loc_oorNextHeadPlus1) begin
      loc_tempIncrNextHead = 3'b000;
    end else begin
      loc_tempIncrNextHead = loc_nextHeadPlus1;
    end
  end

  assign loc_nextEmpty = ((loc_tempIncrNextHead != _zz_loc_nextEmpty) && (loc_nextHead == loc_nextTail));
  assign loc_nextFull = (loc_tempIncrNextHead == _zz_loc_nextFull);
  assign locFormal_testHead = (_zz_locFormal_testHead % 3'b100);
  assign _zz_locFormal_lastTailVal_frameCnt = _zz_loc_arr_port0;
  assign _zz_locFormal_lastTailVal_pos_x = _zz_locFormal_lastTailVal_frameCnt[33 : 2];
  assign _zz_locFormal_lastTailVal_pastPos_x = _zz_locFormal_lastTailVal_frameCnt[65 : 34];
  assign _zz_locFormal_lastTailVal_col_r = _zz_locFormal_lastTailVal_frameCnt[77 : 66];
  assign _zz_io_prev_payload_frameCnt = _zz_loc_arr_port1;
  assign _zz_io_prev_payload_pos_x = _zz_io_prev_payload_frameCnt[33 : 2];
  assign _zz_io_prev_payload_pastPos_x = _zz_io_prev_payload_frameCnt[65 : 34];
  assign _zz_io_prev_payload_col_r = _zz_io_prev_payload_frameCnt[77 : 66];
  assign sbPop_io_prev_payload_frameCnt = _zz_io_prev_payload_frameCnt[1 : 0];
  assign sbPop_io_prev_payload_pos_x = _zz_io_prev_payload_pos_x[15 : 0];
  assign sbPop_io_prev_payload_pos_y = _zz_io_prev_payload_pos_x[31 : 16];
  assign sbPop_io_prev_payload_pastPos_x = _zz_io_prev_payload_pastPos_x[15 : 0];
  assign sbPop_io_prev_payload_pastPos_y = _zz_io_prev_payload_pastPos_x[31 : 16];
  assign sbPop_io_prev_payload_col_r = _zz_io_prev_payload_col_r[3 : 0];
  assign sbPop_io_prev_payload_col_g = _zz_io_prev_payload_col_r[7 : 4];
  assign sbPop_io_prev_payload_col_b = _zz_io_prev_payload_col_r[11 : 8];
  assign rdValid = 1'b1;
  assign io_misc_empty = loc_empty;
  assign io_misc_full = loc_full;
  always @(*) begin
    if(reset) begin
      loc_nextHead = 2'b00;
    end else begin
      if(when_fifoMods_l640) begin
        if(when_fifoMods_l643) begin
          loc_nextHead = 2'b00;
        end else begin
          loc_nextHead = loc_headPlus1[1:0];
        end
      end else begin
        loc_nextHead = loc_head;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      loc_nextTail = 2'b00;
    end else begin
      if(when_fifoMods_l657) begin
        if(when_fifoMods_l658) begin
          loc_nextTail = 2'b00;
        end else begin
          loc_nextTail = loc_tailPlus1[1:0];
        end
      end else begin
        loc_nextTail = loc_tail;
      end
    end
  end

  assign when_fifoMods_l640 = ((! io_misc_full) && wrEn);
  assign when_fifoMods_l643 = (loc_uintDepth <= loc_headPlus1);
  assign when_fifoMods_l657 = ((! io_misc_empty) && rdEn);
  assign when_fifoMods_l658 = (loc_uintDepth <= loc_tailPlus1);
  assign when_fifoMods_l734 = (! reset);
  assign when_fifoMods_l738 = ((! io_misc_full) && wrEn);
  assign _zz_4 = _zz_loc_arr_port3;
  assign _zz_5 = _zz_4[33 : 2];
  assign _zz_6 = _zz_4[65 : 34];
  assign _zz_7 = _zz_4[77 : 66];
  assign switch_fifoMods_l834 = {io_misc_full,io_misc_empty};
  assign when_fifoMods_l857 = (loc_head == loc_tail);
  assign when_fifoMods_l862 = (locFormal_testHead == loc_tail);
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      loc_empty <= 1'b1;
      loc_full <= 1'b0;
      loc_head <= 2'b00;
      loc_tail <= 2'b00;
      locFormal_lastTailVal_frameCnt <= 2'b00;
      locFormal_lastTailVal_pos_x <= 16'h0000;
      locFormal_lastTailVal_pos_y <= 16'h0000;
      locFormal_lastTailVal_pastPos_x <= 16'h0000;
      locFormal_lastTailVal_pastPos_y <= 16'h0000;
      locFormal_lastTailVal_col_r <= 4'b0000;
      locFormal_lastTailVal_col_g <= 4'b0000;
      locFormal_lastTailVal_col_b <= 4'b0000;
    end else begin
      locFormal_lastTailVal_frameCnt <= _zz_locFormal_lastTailVal_frameCnt[1 : 0];
      locFormal_lastTailVal_pos_x <= _zz_locFormal_lastTailVal_pos_x[15 : 0];
      locFormal_lastTailVal_pos_y <= _zz_locFormal_lastTailVal_pos_x[31 : 16];
      locFormal_lastTailVal_pastPos_x <= _zz_locFormal_lastTailVal_pastPos_x[15 : 0];
      locFormal_lastTailVal_pastPos_y <= _zz_locFormal_lastTailVal_pastPos_x[31 : 16];
      locFormal_lastTailVal_col_r <= _zz_locFormal_lastTailVal_col_r[3 : 0];
      locFormal_lastTailVal_col_g <= _zz_locFormal_lastTailVal_col_r[7 : 4];
      locFormal_lastTailVal_col_b <= _zz_locFormal_lastTailVal_col_r[11 : 8];
      loc_head <= loc_nextHead;
      loc_tail <= loc_nextTail;
      loc_empty <= loc_nextEmpty;
      loc_full <= loc_nextFull;
      if(when_fifoMods_l734) begin
        if(formal_with_past_after_reset) begin
          assert((_zz_10 < 3'b100)); // fifoMods.scala:L753
          assert((_zz_11 < 3'b100)); // fifoMods.scala:L756
          assert((! (io_misc_empty && io_misc_full))); // fifoMods.scala:L771
          assert(((((sbPop_io_prev_payload_frameCnt == _zz_4[1 : 0]) && ((sbPop_io_prev_payload_pos_x == _zz_5[15 : 0]) && (sbPop_io_prev_payload_pos_y == _zz_5[31 : 16]))) && ((sbPop_io_prev_payload_pastPos_x == _zz_6[15 : 0]) && (sbPop_io_prev_payload_pastPos_y == _zz_6[31 : 16]))) && (((sbPop_io_prev_payload_col_r == _zz_7[3 : 0]) && (sbPop_io_prev_payload_col_g == _zz_7[7 : 4])) && (sbPop_io_prev_payload_col_b == _zz_7[11 : 8])))); // fifoMods.scala:L773
          if(rdEn_past_1) begin
            if(io_misc_empty_past_1) begin
              assert((loc_tail_regNext == loc_tail)); // fifoMods.scala:L780
            end else begin
              assert((loc_tail == _zz_12)); // fifoMods.scala:L797
            end
          end else begin
            assert((loc_tail_regNext_1 == loc_tail)); // fifoMods.scala:L803
          end
          if(wrEn_past_1) begin
            if(io_misc_full_past_1) begin
              assert((loc_head_regNext == loc_head)); // fifoMods.scala:L810
            end else begin
              assert((loc_head == _zz_14)); // fifoMods.scala:L813
            end
          end else begin
            assert((loc_head_regNext_1 == loc_head)); // fifoMods.scala:L829
          end
          case(switch_fifoMods_l834)
            2'b00 : begin
              assert((loc_head != loc_tail)); // fifoMods.scala:L838
              assert((locFormal_testHead != loc_tail)); // fifoMods.scala:L839
            end
            2'b01 : begin
              assert((loc_head == loc_tail)); // fifoMods.scala:L845
            end
            2'b10 : begin
              assert((locFormal_testHead == loc_tail)); // fifoMods.scala:L851
            end
            default : begin
            end
          endcase
          if(when_fifoMods_l857) begin
            assert(io_misc_empty); // fifoMods.scala:L860
            assert((! io_misc_full)); // fifoMods.scala:L861
          end else begin
            if(when_fifoMods_l862) begin
              assert((! io_misc_empty)); // fifoMods.scala:L865
              assert(io_misc_full); // fifoMods.scala:L866
            end else begin
              assert((! io_misc_empty)); // fifoMods.scala:L870
              assert((! io_misc_full)); // fifoMods.scala:L871
            end
          end
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
    loc_tail_past_1 <= loc_tail;
  end

  always @(posedge clk) begin
    loc_tail_regNext_1 <= loc_tail;
  end

  always @(posedge clk) begin
    io_misc_full_past_1 <= io_misc_full;
  end

  always @(posedge clk) begin
    loc_head_regNext <= loc_head;
  end

  always @(posedge clk) begin
    loc_head_past_1 <= loc_head;
  end

  always @(posedge clk) begin
    loc_head_regNext_1 <= loc_head;
  end


endmodule

module AsyncReadFifo_1 (
  input               io_push_valid,
  output              io_push_ready,
  input      [3:0]    io_push_payload_r,
  input      [3:0]    io_push_payload_g,
  input      [3:0]    io_push_payload_b,
  output              io_pop_valid,
  input               io_pop_ready,
  output     [3:0]    io_pop_payload_r,
  output     [3:0]    io_pop_payload_g,
  output     [3:0]    io_pop_payload_b,
  output              io_misc_empty,
  output              io_misc_full,
  input               reset,
  input               clk
);

  wire                sbPush_io_misc_clear;
  wire       [3:0]    sbPop_io_prev_payload_r;
  wire       [3:0]    sbPop_io_prev_payload_g;
  wire       [3:0]    sbPop_io_prev_payload_b;
  wire                sbPop_io_misc_clear;
  wire       [11:0]   _zz_loc_arr_port0;
  wire       [11:0]   _zz_loc_arr_port1;
  wire       [11:0]   _zz_loc_arr_port3;
  wire                sbPush_io_next_valid;
  wire       [3:0]    sbPush_io_next_payload_r;
  wire       [3:0]    sbPush_io_next_payload_g;
  wire       [3:0]    sbPush_io_next_payload_b;
  wire                sbPush_io_prev_ready;
  wire                sbPop_io_next_valid;
  wire       [3:0]    sbPop_io_next_payload_r;
  wire       [3:0]    sbPop_io_next_payload_g;
  wire       [3:0]    sbPop_io_next_payload_b;
  wire                sbPop_io_prev_ready;
  wire       [5:0]    _zz_loc_tailPlus1;
  wire       [5:0]    _zz_loc_headPlus1;
  wire       [5:0]    _zz_loc_nextHeadPlus1;
  wire       [5:0]    _zz_loc_nextEmpty;
  wire       [5:0]    _zz_loc_nextFull;
  wire       [4:0]    _zz_locFormal_testHead;
  wire       [5:0]    _zz_7;
  wire       [5:0]    _zz_8;
  wire       [4:0]    _zz_9;
  wire       [4:0]    _zz_10;
  wire       [4:0]    _zz_11;
  wire       [4:0]    _zz_12;
  wire       [11:0]   _zz_loc_arr_port;
  reg                 _zz_1;
  wire                wrEn;
  wire                rdValid;
  wire       [3:0]    rdDataPrev_r;
  wire       [3:0]    rdDataPrev_g;
  wire       [3:0]    rdDataPrev_b;
  (* keep *) wire                rdEn;
  wire                fifo_sbPop_io_next_fire;
  reg                 loc_empty;
  reg                 loc_full;
  wire       [5:0]    loc_uintDepth;
  reg        [4:0]    loc_head;
  reg        [4:0]    loc_tail;
  reg        [4:0]    loc_nextHead;
  reg        [4:0]    loc_nextTail;
  (* keep *) wire       [5:0]    loc_tempTailPlus1;
  wire       [5:0]    loc_tailPlus1;
  (* keep *) wire       [5:0]    loc_tempHeadPlus1;
  wire       [5:0]    loc_headPlus1;
  (* keep *) wire       [5:0]    loc_tempNextHeadPlus1;
  wire       [5:0]    loc_nextHeadPlus1;
  (* keep *) wire                loc_tempOorNextHeadPlus1;
  wire                loc_oorNextHeadPlus1;
  (* keep *) reg        [5:0]    loc_tempIncrNextHead;
  wire                loc_nextEmpty;
  wire                loc_nextFull;
  (* keep *) reg        [3:0]    locFormal_lastTailVal_r;
  (* keep *) reg        [3:0]    locFormal_lastTailVal_g;
  (* keep *) reg        [3:0]    locFormal_lastTailVal_b;
  wire       [4:0]    locFormal_testHead;
  wire       [11:0]   _zz_locFormal_lastTailVal_r;
  wire       [11:0]   _zz_io_prev_payload_r;
  wire                when_fifoMods_l640;
  wire                when_fifoMods_l643;
  wire                when_fifoMods_l657;
  wire                when_fifoMods_l658;
  wire                when_fifoMods_l734;
  wire                when_fifoMods_l738;
  reg                 formal_with_past_after_reset;
  wire       [11:0]   _zz_4;
  reg                 rdEn_past_1;
  reg                 io_misc_empty_past_1;
  reg        [4:0]    loc_tail_regNext;
  reg        [4:0]    loc_tail_past_1;
  reg        [4:0]    loc_tail_regNext_1;
  reg                 wrEn_past_1;
  reg                 io_misc_full_past_1;
  reg        [4:0]    loc_head_regNext;
  reg        [4:0]    loc_head_past_1;
  reg        [4:0]    loc_head_regNext_1;
  wire       [1:0]    switch_fifoMods_l834;
  wire                when_fifoMods_l857;
  wire                when_fifoMods_l862;
  (* ram_style = "ultra" , keep *) reg [11:0] loc_arr [0:19];

  assign _zz_loc_tailPlus1 = {1'd0, loc_tail};
  assign _zz_loc_headPlus1 = {1'd0, loc_head};
  assign _zz_loc_nextHeadPlus1 = {1'd0, loc_nextHead};
  assign _zz_loc_nextEmpty = {1'd0, loc_nextTail};
  assign _zz_loc_nextFull = {1'd0, loc_nextTail};
  assign _zz_locFormal_testHead = (loc_head + 5'h01);
  assign _zz_7 = {1'd0, loc_tail};
  assign _zz_8 = {1'd0, loc_head};
  assign _zz_9 = (_zz_10 % 5'h14);
  assign _zz_10 = (loc_tail_past_1 + 5'h01);
  assign _zz_11 = (_zz_12 % 5'h14);
  assign _zz_12 = (loc_head_past_1 + 5'h01);
  assign _zz_loc_arr_port = {io_push_payload_b,{io_push_payload_g,io_push_payload_r}};
  assign _zz_loc_arr_port0 = loc_arr[loc_tail];
  assign _zz_loc_arr_port1 = loc_arr[loc_tail];
  always @(posedge clk) begin
    if(_zz_1) begin
      loc_arr[loc_head] <= _zz_loc_arr_port;
    end
  end

  assign _zz_loc_arr_port3 = loc_arr[loc_tail];
  PipeSkidBuf_3 sbPush (
    .io_next_valid     (sbPush_io_next_valid         ), //o
    .io_next_ready     (1'b1                         ), //i
    .io_next_payload_r (sbPush_io_next_payload_r[3:0]), //o
    .io_next_payload_g (sbPush_io_next_payload_g[3:0]), //o
    .io_next_payload_b (sbPush_io_next_payload_b[3:0]), //o
    .io_prev_valid     (io_push_valid                ), //i
    .io_prev_ready     (sbPush_io_prev_ready         ), //o
    .io_prev_payload_r (io_push_payload_r[3:0]       ), //i
    .io_prev_payload_g (io_push_payload_g[3:0]       ), //i
    .io_prev_payload_b (io_push_payload_b[3:0]       ), //i
    .io_misc_busy      (io_misc_full                 ), //i
    .io_misc_clear     (sbPush_io_misc_clear         ), //i
    .reset             (reset                        ), //i
    .clk               (clk                          )  //i
  );
  PipeSkidBuf_3 sbPop (
    .io_next_valid     (sbPop_io_next_valid         ), //o
    .io_next_ready     (io_pop_ready                ), //i
    .io_next_payload_r (sbPop_io_next_payload_r[3:0]), //o
    .io_next_payload_g (sbPop_io_next_payload_g[3:0]), //o
    .io_next_payload_b (sbPop_io_next_payload_b[3:0]), //o
    .io_prev_valid     (rdValid                     ), //i
    .io_prev_ready     (sbPop_io_prev_ready         ), //o
    .io_prev_payload_r (sbPop_io_prev_payload_r[3:0]), //i
    .io_prev_payload_g (sbPop_io_prev_payload_g[3:0]), //i
    .io_prev_payload_b (sbPop_io_prev_payload_b[3:0]), //i
    .io_misc_busy      (io_misc_empty               ), //i
    .io_misc_clear     (sbPop_io_misc_clear         ), //i
    .reset             (reset                       ), //i
    .clk               (clk                         )  //i
  );
  always @(*) begin
    _zz_1 = 1'b0;
    if(when_fifoMods_l734) begin
      if(when_fifoMods_l738) begin
        _zz_1 = 1'b1;
      end
    end
  end

  assign io_push_ready = sbPush_io_prev_ready;
  assign wrEn = (io_push_valid && sbPush_io_prev_ready);
  assign io_pop_valid = sbPop_io_next_valid;
  assign io_pop_payload_r = sbPop_io_next_payload_r;
  assign io_pop_payload_g = sbPop_io_next_payload_g;
  assign io_pop_payload_b = sbPop_io_next_payload_b;
  assign rdDataPrev_r = 4'b0000;
  assign rdDataPrev_g = 4'b0000;
  assign rdDataPrev_b = 4'b0000;
  assign fifo_sbPop_io_next_fire = (sbPop_io_next_valid && io_pop_ready);
  assign rdEn = fifo_sbPop_io_next_fire;
  assign loc_uintDepth = 6'h14;
  assign loc_tailPlus1 = (_zz_loc_tailPlus1 + 6'h01);
  assign loc_tempTailPlus1 = loc_tailPlus1;
  assign loc_headPlus1 = (_zz_loc_headPlus1 + 6'h01);
  assign loc_tempHeadPlus1 = loc_headPlus1;
  assign loc_nextHeadPlus1 = (_zz_loc_nextHeadPlus1 + 6'h01);
  assign loc_tempNextHeadPlus1 = loc_nextHeadPlus1;
  assign loc_oorNextHeadPlus1 = (loc_uintDepth <= loc_nextHeadPlus1);
  assign loc_tempOorNextHeadPlus1 = loc_oorNextHeadPlus1;
  always @(*) begin
    if(loc_oorNextHeadPlus1) begin
      loc_tempIncrNextHead = 6'h00;
    end else begin
      loc_tempIncrNextHead = loc_nextHeadPlus1;
    end
  end

  assign loc_nextEmpty = ((loc_tempIncrNextHead != _zz_loc_nextEmpty) && (loc_nextHead == loc_nextTail));
  assign loc_nextFull = (loc_tempIncrNextHead == _zz_loc_nextFull);
  assign locFormal_testHead = (_zz_locFormal_testHead % 5'h14);
  assign _zz_locFormal_lastTailVal_r = _zz_loc_arr_port0;
  assign _zz_io_prev_payload_r = _zz_loc_arr_port1;
  assign sbPop_io_prev_payload_r = _zz_io_prev_payload_r[3 : 0];
  assign sbPop_io_prev_payload_g = _zz_io_prev_payload_r[7 : 4];
  assign sbPop_io_prev_payload_b = _zz_io_prev_payload_r[11 : 8];
  assign rdValid = 1'b1;
  assign io_misc_empty = loc_empty;
  assign io_misc_full = loc_full;
  always @(*) begin
    if(reset) begin
      loc_nextHead = 5'h00;
    end else begin
      if(when_fifoMods_l640) begin
        if(when_fifoMods_l643) begin
          loc_nextHead = 5'h00;
        end else begin
          loc_nextHead = loc_headPlus1[4:0];
        end
      end else begin
        loc_nextHead = loc_head;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      loc_nextTail = 5'h00;
    end else begin
      if(when_fifoMods_l657) begin
        if(when_fifoMods_l658) begin
          loc_nextTail = 5'h00;
        end else begin
          loc_nextTail = loc_tailPlus1[4:0];
        end
      end else begin
        loc_nextTail = loc_tail;
      end
    end
  end

  assign when_fifoMods_l640 = ((! io_misc_full) && wrEn);
  assign when_fifoMods_l643 = (loc_uintDepth <= loc_headPlus1);
  assign when_fifoMods_l657 = ((! io_misc_empty) && rdEn);
  assign when_fifoMods_l658 = (loc_uintDepth <= loc_tailPlus1);
  assign when_fifoMods_l734 = (! reset);
  assign when_fifoMods_l738 = ((! io_misc_full) && wrEn);
  assign _zz_4 = _zz_loc_arr_port3;
  assign switch_fifoMods_l834 = {io_misc_full,io_misc_empty};
  assign when_fifoMods_l857 = (loc_head == loc_tail);
  assign when_fifoMods_l862 = (locFormal_testHead == loc_tail);
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      loc_empty <= 1'b1;
      loc_full <= 1'b0;
      loc_head <= 5'h00;
      loc_tail <= 5'h00;
      locFormal_lastTailVal_r <= 4'b0000;
      locFormal_lastTailVal_g <= 4'b0000;
      locFormal_lastTailVal_b <= 4'b0000;
    end else begin
      locFormal_lastTailVal_r <= _zz_locFormal_lastTailVal_r[3 : 0];
      locFormal_lastTailVal_g <= _zz_locFormal_lastTailVal_r[7 : 4];
      locFormal_lastTailVal_b <= _zz_locFormal_lastTailVal_r[11 : 8];
      loc_head <= loc_nextHead;
      loc_tail <= loc_nextTail;
      loc_empty <= loc_nextEmpty;
      loc_full <= loc_nextFull;
      if(when_fifoMods_l734) begin
        if(formal_with_past_after_reset) begin
          assert((_zz_7 < 6'h14)); // fifoMods.scala:L753
          assert((_zz_8 < 6'h14)); // fifoMods.scala:L756
          assert((! (io_misc_empty && io_misc_full))); // fifoMods.scala:L771
          assert((((sbPop_io_prev_payload_r == _zz_4[3 : 0]) && (sbPop_io_prev_payload_g == _zz_4[7 : 4])) && (sbPop_io_prev_payload_b == _zz_4[11 : 8]))); // fifoMods.scala:L773
          if(rdEn_past_1) begin
            if(io_misc_empty_past_1) begin
              assert((loc_tail_regNext == loc_tail)); // fifoMods.scala:L780
            end else begin
              assert((loc_tail == _zz_9)); // fifoMods.scala:L797
            end
          end else begin
            assert((loc_tail_regNext_1 == loc_tail)); // fifoMods.scala:L803
          end
          if(wrEn_past_1) begin
            if(io_misc_full_past_1) begin
              assert((loc_head_regNext == loc_head)); // fifoMods.scala:L810
            end else begin
              assert((loc_head == _zz_11)); // fifoMods.scala:L813
            end
          end else begin
            assert((loc_head_regNext_1 == loc_head)); // fifoMods.scala:L829
          end
          case(switch_fifoMods_l834)
            2'b00 : begin
              assert((loc_head != loc_tail)); // fifoMods.scala:L838
              assert((locFormal_testHead != loc_tail)); // fifoMods.scala:L839
            end
            2'b01 : begin
              assert((loc_head == loc_tail)); // fifoMods.scala:L845
            end
            2'b10 : begin
              assert((locFormal_testHead == loc_tail)); // fifoMods.scala:L851
            end
            default : begin
            end
          endcase
          if(when_fifoMods_l857) begin
            assert(io_misc_empty); // fifoMods.scala:L860
            assert((! io_misc_full)); // fifoMods.scala:L861
          end else begin
            if(when_fifoMods_l862) begin
              assert((! io_misc_empty)); // fifoMods.scala:L865
              assert(io_misc_full); // fifoMods.scala:L866
            end else begin
              assert((! io_misc_empty)); // fifoMods.scala:L870
              assert((! io_misc_full)); // fifoMods.scala:L871
            end
          end
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
    loc_tail_past_1 <= loc_tail;
  end

  always @(posedge clk) begin
    loc_tail_regNext_1 <= loc_tail;
  end

  always @(posedge clk) begin
    io_misc_full_past_1 <= io_misc_full;
  end

  always @(posedge clk) begin
    loc_head_regNext <= loc_head;
  end

  always @(posedge clk) begin
    loc_head_past_1 <= loc_head;
  end

  always @(posedge clk) begin
    loc_head_regNext_1 <= loc_head;
  end


endmodule

//PipeSkidBuf_2 replaced by PipeSkidBuf_1

module PipeSkidBuf_1 (
  (* keep *) output              io_next_valid,
  (* keep *) input               io_next_ready,
  (* keep *) output reg [1:0]    io_next_payload_frameCnt,
  (* keep *) output reg [15:0]   io_next_payload_pos_x,
  (* keep *) output reg [15:0]   io_next_payload_pos_y,
  (* keep *) output reg [15:0]   io_next_payload_pastPos_x,
  (* keep *) output reg [15:0]   io_next_payload_pastPos_y,
  (* keep *) output reg [3:0]    io_next_payload_col_r,
  (* keep *) output reg [3:0]    io_next_payload_col_g,
  (* keep *) output reg [3:0]    io_next_payload_col_b,
  (* keep *) input               io_prev_valid,
  (* keep *) output              io_prev_ready,
  (* keep *) input      [1:0]    io_prev_payload_frameCnt,
  (* keep *) input      [15:0]   io_prev_payload_pos_x,
  (* keep *) input      [15:0]   io_prev_payload_pos_y,
  (* keep *) input      [15:0]   io_prev_payload_pastPos_x,
  (* keep *) input      [15:0]   io_prev_payload_pastPos_y,
  (* keep *) input      [3:0]    io_prev_payload_col_r,
  (* keep *) input      [3:0]    io_prev_payload_col_g,
  (* keep *) input      [3:0]    io_prev_payload_col_b,
  (* keep *) input               io_misc_busy,
  (* keep *) input               io_misc_clear,
  input               reset,
  input               clk
);

  wire                _zz_io_prev_ready;
  (* keep *) wire                _zz_when_Stream_l369;
  (* keep *) wire                _zz_io_prev_ready_1;
  (* keep *) wire       [1:0]    _zz_io_next_payload_frameCnt;
  (* keep *) wire       [15:0]   _zz_io_next_payload_pos_x;
  (* keep *) wire       [15:0]   _zz_io_next_payload_pos_y;
  (* keep *) wire       [15:0]   _zz_io_next_payload_pastPos_x;
  (* keep *) wire       [15:0]   _zz_io_next_payload_pastPos_y;
  (* keep *) wire       [3:0]    _zz_io_next_payload_col_r;
  (* keep *) wire       [3:0]    _zz_io_next_payload_col_g;
  (* keep *) wire       [3:0]    _zz_io_next_payload_col_b;
  (* keep *) wire                _zz_io_next_valid;
  (* keep *) wire                _zz_1;
  (* keep *) wire       [1:0]    _zz_io_next_payload_frameCnt_1;
  (* keep *) wire       [15:0]   _zz_io_next_payload_pos_x_1;
  (* keep *) wire       [15:0]   _zz_io_next_payload_pos_y_1;
  (* keep *) wire       [15:0]   _zz_io_next_payload_pastPos_x_1;
  (* keep *) wire       [15:0]   _zz_io_next_payload_pastPos_y_1;
  (* keep *) wire       [3:0]    _zz_io_next_payload_col_r_1;
  (* keep *) wire       [3:0]    _zz_io_next_payload_col_g_1;
  (* keep *) wire       [3:0]    _zz_io_next_payload_col_b_1;
  reg                 _zz_2;
  reg                 _zz_io_prev_ready_2;
  reg        [1:0]    _zz_io_next_payload_frameCnt_2;
  reg        [15:0]   _zz_io_next_payload_pos_x_2;
  reg        [15:0]   _zz_io_next_payload_pos_y_2;
  reg        [15:0]   _zz_io_next_payload_pastPos_x_2;
  reg        [15:0]   _zz_io_next_payload_pastPos_y_2;
  reg        [3:0]    _zz_io_next_payload_col_r_2;
  reg        [3:0]    _zz_io_next_payload_col_g_2;
  reg        [3:0]    _zz_io_next_payload_col_b_2;
  wire                _zz_when_Stream_l369_1;
  reg                 _zz_when_Stream_l369_2;
  reg        [1:0]    _zz_io_next_payload_frameCnt_3;
  reg        [15:0]   _zz_io_next_payload_pos_x_3;
  reg        [15:0]   _zz_io_next_payload_pos_y_3;
  reg        [15:0]   _zz_io_next_payload_pastPos_x_3;
  reg        [15:0]   _zz_io_next_payload_pastPos_y_3;
  reg        [3:0]    _zz_io_next_payload_col_r_3;
  reg        [3:0]    _zz_io_next_payload_col_g_3;
  reg        [3:0]    _zz_io_next_payload_col_b_3;
  wire                when_Stream_l369;
  wire                io_prev_fire;
  reg                 formal_with_past_after_reset;
  reg                 _zz_3;
  reg        [1:0]    _zz_4;
  reg        [15:0]   _zz_5;
  reg        [15:0]   _zz_6;
  reg        [15:0]   _zz_7;
  reg        [15:0]   _zz_8;
  reg        [3:0]    _zz_9;
  reg        [3:0]    _zz_10;
  reg        [3:0]    _zz_11;
  wire                io_next_isStall;
  reg                 io_next_isStall_past_1;
  reg        [1:0]    io_next_payload_regNext_frameCnt;
  reg        [15:0]   io_next_payload_regNext_pos_x;
  reg        [15:0]   io_next_payload_regNext_pos_y;
  reg        [15:0]   io_next_payload_regNext_pastPos_x;
  reg        [15:0]   io_next_payload_regNext_pastPos_y;
  reg        [3:0]    io_next_payload_regNext_col_r;
  reg        [3:0]    io_next_payload_regNext_col_g;
  reg        [3:0]    io_next_payload_regNext_col_b;

  assign _zz_io_prev_ready = (! io_misc_busy);
  assign _zz_when_Stream_l369 = (io_prev_valid && _zz_io_prev_ready);
  assign io_prev_ready = (_zz_io_prev_ready_1 && _zz_io_prev_ready);
  assign _zz_io_next_payload_frameCnt = io_prev_payload_frameCnt;
  assign _zz_io_next_payload_pos_x = io_prev_payload_pos_x;
  assign _zz_io_next_payload_pos_y = io_prev_payload_pos_y;
  assign _zz_io_next_payload_pastPos_x = io_prev_payload_pastPos_x;
  assign _zz_io_next_payload_pastPos_y = io_prev_payload_pastPos_y;
  assign _zz_io_next_payload_col_r = io_prev_payload_col_r;
  assign _zz_io_next_payload_col_g = io_prev_payload_col_g;
  assign _zz_io_next_payload_col_b = io_prev_payload_col_b;
  assign _zz_io_prev_ready_1 = _zz_io_prev_ready_2;
  always @(*) begin
    _zz_2 = _zz_1;
    if(when_Stream_l369) begin
      _zz_2 = 1'b1;
    end
  end

  assign when_Stream_l369 = (! _zz_when_Stream_l369_1);
  assign _zz_when_Stream_l369_1 = _zz_when_Stream_l369_2;
  assign _zz_io_next_valid = _zz_when_Stream_l369_1;
  assign _zz_io_next_payload_frameCnt_1 = _zz_io_next_payload_frameCnt_3;
  assign _zz_io_next_payload_pos_x_1 = _zz_io_next_payload_pos_x_3;
  assign _zz_io_next_payload_pos_y_1 = _zz_io_next_payload_pos_y_3;
  assign _zz_io_next_payload_pastPos_x_1 = _zz_io_next_payload_pastPos_x_3;
  assign _zz_io_next_payload_pastPos_y_1 = _zz_io_next_payload_pastPos_y_3;
  assign _zz_io_next_payload_col_r_1 = _zz_io_next_payload_col_r_3;
  assign _zz_io_next_payload_col_g_1 = _zz_io_next_payload_col_g_3;
  assign _zz_io_next_payload_col_b_1 = _zz_io_next_payload_col_b_3;
  assign io_next_valid = _zz_io_next_valid;
  assign _zz_1 = io_next_ready;
  always @(*) begin
    if(reset) begin
      io_next_payload_frameCnt = 2'b00;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_frameCnt = _zz_io_next_payload_frameCnt_1;
      end else begin
        io_next_payload_frameCnt = 2'b00;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      io_next_payload_pos_x = 16'h0000;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_pos_x = _zz_io_next_payload_pos_x_1;
      end else begin
        io_next_payload_pos_x = 16'h0000;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      io_next_payload_pos_y = 16'h0000;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_pos_y = _zz_io_next_payload_pos_y_1;
      end else begin
        io_next_payload_pos_y = 16'h0000;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      io_next_payload_pastPos_x = 16'h0000;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_pastPos_x = _zz_io_next_payload_pastPos_x_1;
      end else begin
        io_next_payload_pastPos_x = 16'h0000;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      io_next_payload_pastPos_y = 16'h0000;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_pastPos_y = _zz_io_next_payload_pastPos_y_1;
      end else begin
        io_next_payload_pastPos_y = 16'h0000;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      io_next_payload_col_r = 4'b0000;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_col_r = _zz_io_next_payload_col_r_1;
      end else begin
        io_next_payload_col_r = 4'b0000;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      io_next_payload_col_g = 4'b0000;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_col_g = _zz_io_next_payload_col_g_1;
      end else begin
        io_next_payload_col_g = 4'b0000;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      io_next_payload_col_b = 4'b0000;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_col_b = _zz_io_next_payload_col_b_1;
      end else begin
        io_next_payload_col_b = 4'b0000;
      end
    end
  end

  assign io_prev_fire = (io_prev_valid && io_prev_ready);
  assign io_next_isStall = (io_next_valid && (! io_next_ready));
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      _zz_io_prev_ready_2 <= 1'b1;
      _zz_when_Stream_l369_2 <= 1'b0;
      formal_with_past_after_reset <= 1'b0;
    end else begin
      if(_zz_when_Stream_l369) begin
        _zz_io_prev_ready_2 <= 1'b0;
      end
      if(_zz_2) begin
        _zz_io_prev_ready_2 <= 1'b1;
      end
      if(_zz_2) begin
        _zz_when_Stream_l369_2 <= (_zz_when_Stream_l369 || (! _zz_io_prev_ready_2));
      end
      formal_with_past_after_reset <= (! reset);
      if(formal_with_past_after_reset) begin
        if(_zz_3) begin
          assert(_zz_io_next_valid); // pipelineMods.scala:L253
          assert(((((_zz_4 == _zz_io_next_payload_frameCnt_1) && ((_zz_5 == _zz_io_next_payload_pos_x_1) && (_zz_6 == _zz_io_next_payload_pos_y_1))) && ((_zz_7 == _zz_io_next_payload_pastPos_x_1) && (_zz_8 == _zz_io_next_payload_pastPos_y_1))) && (((_zz_9 == _zz_io_next_payload_col_r_1) && (_zz_10 == _zz_io_next_payload_col_g_1)) && (_zz_11 == _zz_io_next_payload_col_b_1)))); // pipelineMods.scala:L253
        end
        if(io_next_isStall_past_1) begin
          assume(io_next_valid); // pipelineMods.scala:L255
          assume(((((io_next_payload_regNext_frameCnt == io_next_payload_frameCnt) && ((io_next_payload_regNext_pos_x == io_next_payload_pos_x) && (io_next_payload_regNext_pos_y == io_next_payload_pos_y))) && ((io_next_payload_regNext_pastPos_x == io_next_payload_pastPos_x) && (io_next_payload_regNext_pastPos_y == io_next_payload_pastPos_y))) && (((io_next_payload_regNext_col_r == io_next_payload_col_r) && (io_next_payload_regNext_col_g == io_next_payload_col_g)) && (io_next_payload_regNext_col_b == io_next_payload_col_b)))); // pipelineMods.scala:L255
        end
      end
    end
  end

  always @(posedge clk) begin
    if(_zz_io_prev_ready_1) begin
      _zz_io_next_payload_frameCnt_2 <= _zz_io_next_payload_frameCnt;
      _zz_io_next_payload_pos_x_2 <= _zz_io_next_payload_pos_x;
      _zz_io_next_payload_pos_y_2 <= _zz_io_next_payload_pos_y;
      _zz_io_next_payload_pastPos_x_2 <= _zz_io_next_payload_pastPos_x;
      _zz_io_next_payload_pastPos_y_2 <= _zz_io_next_payload_pastPos_y;
      _zz_io_next_payload_col_r_2 <= _zz_io_next_payload_col_r;
      _zz_io_next_payload_col_g_2 <= _zz_io_next_payload_col_g;
      _zz_io_next_payload_col_b_2 <= _zz_io_next_payload_col_b;
    end
    if(_zz_2) begin
      _zz_io_next_payload_frameCnt_3 <= (_zz_io_prev_ready_2 ? _zz_io_next_payload_frameCnt : _zz_io_next_payload_frameCnt_2);
      _zz_io_next_payload_pos_x_3 <= (_zz_io_prev_ready_2 ? _zz_io_next_payload_pos_x : _zz_io_next_payload_pos_x_2);
      _zz_io_next_payload_pos_y_3 <= (_zz_io_prev_ready_2 ? _zz_io_next_payload_pos_y : _zz_io_next_payload_pos_y_2);
      _zz_io_next_payload_pastPos_x_3 <= (_zz_io_prev_ready_2 ? _zz_io_next_payload_pastPos_x : _zz_io_next_payload_pastPos_x_2);
      _zz_io_next_payload_pastPos_y_3 <= (_zz_io_prev_ready_2 ? _zz_io_next_payload_pastPos_y : _zz_io_next_payload_pastPos_y_2);
      _zz_io_next_payload_col_r_3 <= (_zz_io_prev_ready_2 ? _zz_io_next_payload_col_r : _zz_io_next_payload_col_r_2);
      _zz_io_next_payload_col_g_3 <= (_zz_io_prev_ready_2 ? _zz_io_next_payload_col_g : _zz_io_next_payload_col_g_2);
      _zz_io_next_payload_col_b_3 <= (_zz_io_prev_ready_2 ? _zz_io_next_payload_col_b : _zz_io_next_payload_col_b_2);
    end
  end

  always @(posedge clk or posedge reset) begin
    if(reset) begin
      _zz_3 <= 1'b0;
      io_next_isStall_past_1 <= 1'b0;
    end else begin
      _zz_3 <= (_zz_io_next_valid && (! _zz_1));
      io_next_isStall_past_1 <= io_next_isStall;
    end
  end

  always @(posedge clk) begin
    _zz_4 <= _zz_io_next_payload_frameCnt_1;
    _zz_5 <= _zz_io_next_payload_pos_x_1;
    _zz_6 <= _zz_io_next_payload_pos_y_1;
    _zz_7 <= _zz_io_next_payload_pastPos_x_1;
    _zz_8 <= _zz_io_next_payload_pastPos_y_1;
    _zz_9 <= _zz_io_next_payload_col_r_1;
    _zz_10 <= _zz_io_next_payload_col_g_1;
    _zz_11 <= _zz_io_next_payload_col_b_1;
  end

  always @(posedge clk) begin
    io_next_payload_regNext_frameCnt <= io_next_payload_frameCnt;
    io_next_payload_regNext_pos_x <= io_next_payload_pos_x;
    io_next_payload_regNext_pos_y <= io_next_payload_pos_y;
    io_next_payload_regNext_pastPos_x <= io_next_payload_pastPos_x;
    io_next_payload_regNext_pastPos_y <= io_next_payload_pastPos_y;
    io_next_payload_regNext_col_r <= io_next_payload_col_r;
    io_next_payload_regNext_col_g <= io_next_payload_col_g;
    io_next_payload_regNext_col_b <= io_next_payload_col_b;
  end


endmodule

//PipeSkidBuf_4 replaced by PipeSkidBuf_3

module PipeSkidBuf_3 (
  (* keep *) output              io_next_valid,
  (* keep *) input               io_next_ready,
  (* keep *) output reg [3:0]    io_next_payload_r,
  (* keep *) output reg [3:0]    io_next_payload_g,
  (* keep *) output reg [3:0]    io_next_payload_b,
  (* keep *) input               io_prev_valid,
  (* keep *) output              io_prev_ready,
  (* keep *) input      [3:0]    io_prev_payload_r,
  (* keep *) input      [3:0]    io_prev_payload_g,
  (* keep *) input      [3:0]    io_prev_payload_b,
  (* keep *) input               io_misc_busy,
  (* keep *) input               io_misc_clear,
  input               reset,
  input               clk
);

  wire                _zz_io_prev_ready;
  (* keep *) wire                _zz_when_Stream_l369;
  (* keep *) wire                _zz_io_prev_ready_1;
  (* keep *) wire       [3:0]    _zz_io_next_payload_r;
  (* keep *) wire       [3:0]    _zz_io_next_payload_g;
  (* keep *) wire       [3:0]    _zz_io_next_payload_b;
  (* keep *) wire                _zz_io_next_valid;
  (* keep *) wire                _zz_1;
  (* keep *) wire       [3:0]    _zz_io_next_payload_r_1;
  (* keep *) wire       [3:0]    _zz_io_next_payload_g_1;
  (* keep *) wire       [3:0]    _zz_io_next_payload_b_1;
  reg                 _zz_2;
  reg                 _zz_io_prev_ready_2;
  reg        [3:0]    _zz_io_next_payload_r_2;
  reg        [3:0]    _zz_io_next_payload_g_2;
  reg        [3:0]    _zz_io_next_payload_b_2;
  wire                _zz_when_Stream_l369_1;
  reg                 _zz_when_Stream_l369_2;
  reg        [3:0]    _zz_io_next_payload_r_3;
  reg        [3:0]    _zz_io_next_payload_g_3;
  reg        [3:0]    _zz_io_next_payload_b_3;
  wire                when_Stream_l369;
  wire                io_prev_fire;
  reg                 formal_with_past_after_reset;
  reg                 _zz_3;
  reg        [3:0]    _zz_4;
  reg        [3:0]    _zz_5;
  reg        [3:0]    _zz_6;
  wire                io_next_isStall;
  reg                 io_next_isStall_past_1;
  reg        [3:0]    io_next_payload_regNext_r;
  reg        [3:0]    io_next_payload_regNext_g;
  reg        [3:0]    io_next_payload_regNext_b;

  assign _zz_io_prev_ready = (! io_misc_busy);
  assign _zz_when_Stream_l369 = (io_prev_valid && _zz_io_prev_ready);
  assign io_prev_ready = (_zz_io_prev_ready_1 && _zz_io_prev_ready);
  assign _zz_io_next_payload_r = io_prev_payload_r;
  assign _zz_io_next_payload_g = io_prev_payload_g;
  assign _zz_io_next_payload_b = io_prev_payload_b;
  assign _zz_io_prev_ready_1 = _zz_io_prev_ready_2;
  always @(*) begin
    _zz_2 = _zz_1;
    if(when_Stream_l369) begin
      _zz_2 = 1'b1;
    end
  end

  assign when_Stream_l369 = (! _zz_when_Stream_l369_1);
  assign _zz_when_Stream_l369_1 = _zz_when_Stream_l369_2;
  assign _zz_io_next_valid = _zz_when_Stream_l369_1;
  assign _zz_io_next_payload_r_1 = _zz_io_next_payload_r_3;
  assign _zz_io_next_payload_g_1 = _zz_io_next_payload_g_3;
  assign _zz_io_next_payload_b_1 = _zz_io_next_payload_b_3;
  assign io_next_valid = _zz_io_next_valid;
  assign _zz_1 = io_next_ready;
  always @(*) begin
    if(reset) begin
      io_next_payload_r = 4'b0000;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_r = _zz_io_next_payload_r_1;
      end else begin
        io_next_payload_r = 4'b0000;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      io_next_payload_g = 4'b0000;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_g = _zz_io_next_payload_g_1;
      end else begin
        io_next_payload_g = 4'b0000;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      io_next_payload_b = 4'b0000;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_b = _zz_io_next_payload_b_1;
      end else begin
        io_next_payload_b = 4'b0000;
      end
    end
  end

  assign io_prev_fire = (io_prev_valid && io_prev_ready);
  assign io_next_isStall = (io_next_valid && (! io_next_ready));
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      _zz_io_prev_ready_2 <= 1'b1;
      _zz_when_Stream_l369_2 <= 1'b0;
      formal_with_past_after_reset <= 1'b0;
    end else begin
      if(_zz_when_Stream_l369) begin
        _zz_io_prev_ready_2 <= 1'b0;
      end
      if(_zz_2) begin
        _zz_io_prev_ready_2 <= 1'b1;
      end
      if(_zz_2) begin
        _zz_when_Stream_l369_2 <= (_zz_when_Stream_l369 || (! _zz_io_prev_ready_2));
      end
      formal_with_past_after_reset <= (! reset);
      if(formal_with_past_after_reset) begin
        if(_zz_3) begin
          assert(_zz_io_next_valid); // pipelineMods.scala:L253
          assert((((_zz_4 == _zz_io_next_payload_r_1) && (_zz_5 == _zz_io_next_payload_g_1)) && (_zz_6 == _zz_io_next_payload_b_1))); // pipelineMods.scala:L253
        end
        if(io_next_isStall_past_1) begin
          assume(io_next_valid); // pipelineMods.scala:L255
          assume((((io_next_payload_regNext_r == io_next_payload_r) && (io_next_payload_regNext_g == io_next_payload_g)) && (io_next_payload_regNext_b == io_next_payload_b))); // pipelineMods.scala:L255
        end
      end
    end
  end

  always @(posedge clk) begin
    if(_zz_io_prev_ready_1) begin
      _zz_io_next_payload_r_2 <= _zz_io_next_payload_r;
      _zz_io_next_payload_g_2 <= _zz_io_next_payload_g;
      _zz_io_next_payload_b_2 <= _zz_io_next_payload_b;
    end
    if(_zz_2) begin
      _zz_io_next_payload_r_3 <= (_zz_io_prev_ready_2 ? _zz_io_next_payload_r : _zz_io_next_payload_r_2);
      _zz_io_next_payload_g_3 <= (_zz_io_prev_ready_2 ? _zz_io_next_payload_g : _zz_io_next_payload_g_2);
      _zz_io_next_payload_b_3 <= (_zz_io_prev_ready_2 ? _zz_io_next_payload_b : _zz_io_next_payload_b_2);
    end
  end

  always @(posedge clk or posedge reset) begin
    if(reset) begin
      _zz_3 <= 1'b0;
      io_next_isStall_past_1 <= 1'b0;
    end else begin
      _zz_3 <= (_zz_io_next_valid && (! _zz_1));
      io_next_isStall_past_1 <= io_next_isStall;
    end
  end

  always @(posedge clk) begin
    _zz_4 <= _zz_io_next_payload_r_1;
    _zz_5 <= _zz_io_next_payload_g_1;
    _zz_6 <= _zz_io_next_payload_b_1;
  end

  always @(posedge clk) begin
    io_next_payload_regNext_r <= io_next_payload_r;
    io_next_payload_regNext_g <= io_next_payload_g;
    io_next_payload_regNext_b <= io_next_payload_b;
  end


endmodule
