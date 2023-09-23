// Generator : SpinalHDL v1.9.3    git head : 029104c77a54c53f1edda327a3bea333f7d65fd9
// Component : Dut
// Git hash  : 362013a33592715f2e304985be27aaa7a2ef094e

`timescale 1ns/1ps

module Dut (
  output     [1:0]    io_phys_col_r,
  output     [1:0]    io_phys_col_g,
  output     [1:0]    io_phys_col_b,
  output              io_phys_hsync,
  output              io_phys_vsync,
  output     [1:0]    io_misc_hscS,
  output     [4:0]    io_misc_hscC,
  output     [1:0]    io_misc_hscNextS,
  output     [1:0]    io_misc_vscS,
  output     [4:0]    io_misc_vscC,
  output     [1:0]    io_misc_vscNextS,
  output              io_misc_fifoEmpty,
  output              io_misc_fifoFull,
  output              io_misc_nextPixelEn,
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

  wire       [1:0]    vgaGrad_io_vgaCtrlIo_phys_col_r;
  wire       [1:0]    vgaGrad_io_vgaCtrlIo_phys_col_g;
  wire       [1:0]    vgaGrad_io_vgaCtrlIo_phys_col_b;
  wire                vgaGrad_io_vgaCtrlIo_phys_hsync;
  wire                vgaGrad_io_vgaCtrlIo_phys_vsync;
  wire                vgaCtrl_io_push_ready;
  wire       [1:0]    vgaCtrl_io_phys_col_r;
  wire       [1:0]    vgaCtrl_io_phys_col_g;
  wire       [1:0]    vgaCtrl_io_phys_col_b;
  wire                vgaCtrl_io_phys_hsync;
  wire                vgaCtrl_io_phys_vsync;
  wire       [1:0]    vgaCtrl_io_misc_hscS;
  wire       [4:0]    vgaCtrl_io_misc_hscC;
  wire       [1:0]    vgaCtrl_io_misc_hscNextS;
  wire       [1:0]    vgaCtrl_io_misc_vscS;
  wire       [4:0]    vgaCtrl_io_misc_vscC;
  wire       [1:0]    vgaCtrl_io_misc_vscNextS;
  wire                vgaCtrl_io_misc_fifoEmpty;
  wire                vgaCtrl_io_misc_fifoFull;
  wire                vgaCtrl_io_misc_nextPixelEn;
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
  wire       [1:0]    vidDith_io_outp_frameCnt;
  wire       [15:0]   vidDith_io_outp_nextPos_x;
  wire       [15:0]   vidDith_io_outp_nextPos_y;
  wire       [15:0]   vidDith_io_outp_pos_x;
  wire       [15:0]   vidDith_io_outp_pos_y;
  wire       [15:0]   vidDith_io_outp_pastPos_x;
  wire       [15:0]   vidDith_io_outp_pastPos_y;
  wire       [1:0]    vidDith_io_outp_col_r;
  wire       [1:0]    vidDith_io_outp_col_g;
  wire       [1:0]    vidDith_io_outp_col_b;
  wire                vgaGrad_io_vgaCtrlIo_en;
  wire                vgaGrad_io_vgaCtrlIo_push_valid;
  wire       [1:0]    vgaGrad_io_vgaCtrlIo_push_payload_r;
  wire       [1:0]    vgaGrad_io_vgaCtrlIo_push_payload_g;
  wire       [1:0]    vgaGrad_io_vgaCtrlIo_push_payload_b;
  wire                vgaGrad_io_vidDithIo_push_valid;
  wire       [3:0]    vgaGrad_io_vidDithIo_push_payload_r;
  wire       [3:0]    vgaGrad_io_vidDithIo_push_payload_g;
  wire       [3:0]    vgaGrad_io_vidDithIo_push_payload_b;
  `ifndef SYNTHESIS
  reg [39:0] io_misc_hscS_string;
  reg [39:0] io_misc_hscNextS_string;
  reg [39:0] io_misc_vscS_string;
  reg [39:0] io_misc_vscNextS_string;
  `endif


  LcvVgaCtrl vgaCtrl (
    .io_en                 (vgaGrad_io_vgaCtrlIo_en                 ), //i
    .io_push_valid         (vgaGrad_io_vgaCtrlIo_push_valid         ), //i
    .io_push_ready         (vgaCtrl_io_push_ready                   ), //o
    .io_push_payload_r     (vgaGrad_io_vgaCtrlIo_push_payload_r[1:0]), //i
    .io_push_payload_g     (vgaGrad_io_vgaCtrlIo_push_payload_g[1:0]), //i
    .io_push_payload_b     (vgaGrad_io_vgaCtrlIo_push_payload_b[1:0]), //i
    .io_phys_col_r         (vgaCtrl_io_phys_col_r[1:0]              ), //o
    .io_phys_col_g         (vgaCtrl_io_phys_col_g[1:0]              ), //o
    .io_phys_col_b         (vgaCtrl_io_phys_col_b[1:0]              ), //o
    .io_phys_hsync         (vgaCtrl_io_phys_hsync                   ), //o
    .io_phys_vsync         (vgaCtrl_io_phys_vsync                   ), //o
    .io_misc_hscS          (vgaCtrl_io_misc_hscS[1:0]               ), //o
    .io_misc_hscC          (vgaCtrl_io_misc_hscC[4:0]               ), //o
    .io_misc_hscNextS      (vgaCtrl_io_misc_hscNextS[1:0]           ), //o
    .io_misc_vscS          (vgaCtrl_io_misc_vscS[1:0]               ), //o
    .io_misc_vscC          (vgaCtrl_io_misc_vscC[4:0]               ), //o
    .io_misc_vscNextS      (vgaCtrl_io_misc_vscNextS[1:0]           ), //o
    .io_misc_fifoEmpty     (vgaCtrl_io_misc_fifoEmpty               ), //o
    .io_misc_fifoFull      (vgaCtrl_io_misc_fifoFull                ), //o
    .io_misc_nextPixelEn   (vgaCtrl_io_misc_nextPixelEn             ), //o
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
    .io_push_valid     (vgaGrad_io_vidDithIo_push_valid         ), //i
    .io_push_payload_r (vgaGrad_io_vidDithIo_push_payload_r[3:0]), //i
    .io_push_payload_g (vgaGrad_io_vidDithIo_push_payload_g[3:0]), //i
    .io_push_payload_b (vgaGrad_io_vidDithIo_push_payload_b[3:0]), //i
    .io_outp_frameCnt  (vidDith_io_outp_frameCnt[1:0]           ), //o
    .io_outp_nextPos_x (vidDith_io_outp_nextPos_x[15:0]         ), //o
    .io_outp_nextPos_y (vidDith_io_outp_nextPos_y[15:0]         ), //o
    .io_outp_pos_x     (vidDith_io_outp_pos_x[15:0]             ), //o
    .io_outp_pos_y     (vidDith_io_outp_pos_y[15:0]             ), //o
    .io_outp_pastPos_x (vidDith_io_outp_pastPos_x[15:0]         ), //o
    .io_outp_pastPos_y (vidDith_io_outp_pastPos_y[15:0]         ), //o
    .io_outp_col_r     (vidDith_io_outp_col_r[1:0]              ), //o
    .io_outp_col_g     (vidDith_io_outp_col_g[1:0]              ), //o
    .io_outp_col_b     (vidDith_io_outp_col_b[1:0]              ), //o
    .clk               (clk                                     ), //i
    .reset             (reset                                   )  //i
  );
  LcvVgaGradient vgaGrad (
    .io_vgaCtrlIo_en                 (vgaGrad_io_vgaCtrlIo_en                 ), //o
    .io_vgaCtrlIo_push_valid         (vgaGrad_io_vgaCtrlIo_push_valid         ), //o
    .io_vgaCtrlIo_push_ready         (vgaCtrl_io_push_ready                   ), //i
    .io_vgaCtrlIo_push_payload_r     (vgaGrad_io_vgaCtrlIo_push_payload_r[1:0]), //o
    .io_vgaCtrlIo_push_payload_g     (vgaGrad_io_vgaCtrlIo_push_payload_g[1:0]), //o
    .io_vgaCtrlIo_push_payload_b     (vgaGrad_io_vgaCtrlIo_push_payload_b[1:0]), //o
    .io_vgaCtrlIo_phys_col_r         (vgaGrad_io_vgaCtrlIo_phys_col_r[1:0]    ), //i
    .io_vgaCtrlIo_phys_col_g         (vgaGrad_io_vgaCtrlIo_phys_col_g[1:0]    ), //i
    .io_vgaCtrlIo_phys_col_b         (vgaGrad_io_vgaCtrlIo_phys_col_b[1:0]    ), //i
    .io_vgaCtrlIo_phys_hsync         (vgaGrad_io_vgaCtrlIo_phys_hsync         ), //i
    .io_vgaCtrlIo_phys_vsync         (vgaGrad_io_vgaCtrlIo_phys_vsync         ), //i
    .io_vgaCtrlIo_misc_hscS          (vgaCtrl_io_misc_hscS[1:0]               ), //i
    .io_vgaCtrlIo_misc_hscC          (vgaCtrl_io_misc_hscC[4:0]               ), //i
    .io_vgaCtrlIo_misc_hscNextS      (vgaCtrl_io_misc_hscNextS[1:0]           ), //i
    .io_vgaCtrlIo_misc_vscS          (vgaCtrl_io_misc_vscS[1:0]               ), //i
    .io_vgaCtrlIo_misc_vscC          (vgaCtrl_io_misc_vscC[4:0]               ), //i
    .io_vgaCtrlIo_misc_vscNextS      (vgaCtrl_io_misc_vscNextS[1:0]           ), //i
    .io_vgaCtrlIo_misc_fifoEmpty     (vgaCtrl_io_misc_fifoEmpty               ), //i
    .io_vgaCtrlIo_misc_fifoFull      (vgaCtrl_io_misc_fifoFull                ), //i
    .io_vgaCtrlIo_misc_nextPixelEn   (vgaCtrl_io_misc_nextPixelEn             ), //i
    .io_vgaCtrlIo_misc_pixelEn       (vgaCtrl_io_misc_pixelEn                 ), //i
    .io_vgaCtrlIo_misc_nextVisib     (vgaCtrl_io_misc_nextVisib               ), //i
    .io_vgaCtrlIo_misc_visib         (vgaCtrl_io_misc_visib                   ), //i
    .io_vgaCtrlIo_misc_pastVisib     (vgaCtrl_io_misc_pastVisib               ), //i
    .io_vgaCtrlIo_misc_drawPos_x     (vgaCtrl_io_misc_drawPos_x[15:0]         ), //i
    .io_vgaCtrlIo_misc_drawPos_y     (vgaCtrl_io_misc_drawPos_y[15:0]         ), //i
    .io_vgaCtrlIo_misc_pastDrawPos_x (vgaCtrl_io_misc_pastDrawPos_x[15:0]     ), //i
    .io_vgaCtrlIo_misc_pastDrawPos_y (vgaCtrl_io_misc_pastDrawPos_y[15:0]     ), //i
    .io_vgaCtrlIo_misc_size_x        (vgaCtrl_io_misc_size_x[15:0]            ), //i
    .io_vgaCtrlIo_misc_size_y        (vgaCtrl_io_misc_size_y[15:0]            ), //i
    .io_vidDithIo_push_valid         (vgaGrad_io_vidDithIo_push_valid         ), //o
    .io_vidDithIo_push_payload_r     (vgaGrad_io_vidDithIo_push_payload_r[3:0]), //o
    .io_vidDithIo_push_payload_g     (vgaGrad_io_vidDithIo_push_payload_g[3:0]), //o
    .io_vidDithIo_push_payload_b     (vgaGrad_io_vidDithIo_push_payload_b[3:0]), //o
    .io_vidDithIo_outp_frameCnt      (vidDith_io_outp_frameCnt[1:0]           ), //i
    .io_vidDithIo_outp_nextPos_x     (vidDith_io_outp_nextPos_x[15:0]         ), //i
    .io_vidDithIo_outp_nextPos_y     (vidDith_io_outp_nextPos_y[15:0]         ), //i
    .io_vidDithIo_outp_pos_x         (vidDith_io_outp_pos_x[15:0]             ), //i
    .io_vidDithIo_outp_pos_y         (vidDith_io_outp_pos_y[15:0]             ), //i
    .io_vidDithIo_outp_pastPos_x     (vidDith_io_outp_pastPos_x[15:0]         ), //i
    .io_vidDithIo_outp_pastPos_y     (vidDith_io_outp_pastPos_y[15:0]         ), //i
    .io_vidDithIo_outp_col_r         (vidDith_io_outp_col_r[1:0]              ), //i
    .io_vidDithIo_outp_col_g         (vidDith_io_outp_col_g[1:0]              ), //i
    .io_vidDithIo_outp_col_b         (vidDith_io_outp_col_b[1:0]              ), //i
    .clk                             (clk                                     ), //i
    .reset                           (reset                                   )  //i
  );
  `ifndef SYNTHESIS
  always @(*) begin
    case(io_misc_hscS)
      LcvVgaState_front : io_misc_hscS_string = "front";
      LcvVgaState_sync : io_misc_hscS_string = "sync ";
      LcvVgaState_back : io_misc_hscS_string = "back ";
      LcvVgaState_visib : io_misc_hscS_string = "visib";
      default : io_misc_hscS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_misc_hscNextS)
      LcvVgaState_front : io_misc_hscNextS_string = "front";
      LcvVgaState_sync : io_misc_hscNextS_string = "sync ";
      LcvVgaState_back : io_misc_hscNextS_string = "back ";
      LcvVgaState_visib : io_misc_hscNextS_string = "visib";
      default : io_misc_hscNextS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_misc_vscS)
      LcvVgaState_front : io_misc_vscS_string = "front";
      LcvVgaState_sync : io_misc_vscS_string = "sync ";
      LcvVgaState_back : io_misc_vscS_string = "back ";
      LcvVgaState_visib : io_misc_vscS_string = "visib";
      default : io_misc_vscS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_misc_vscNextS)
      LcvVgaState_front : io_misc_vscNextS_string = "front";
      LcvVgaState_sync : io_misc_vscNextS_string = "sync ";
      LcvVgaState_back : io_misc_vscNextS_string = "back ";
      LcvVgaState_visib : io_misc_vscNextS_string = "visib";
      default : io_misc_vscNextS_string = "?????";
    endcase
  end
  `endif

  assign io_phys_col_r = vgaCtrl_io_phys_col_r;
  assign io_phys_col_g = vgaCtrl_io_phys_col_g;
  assign io_phys_col_b = vgaCtrl_io_phys_col_b;
  assign io_phys_hsync = vgaCtrl_io_phys_hsync;
  assign io_phys_vsync = vgaCtrl_io_phys_vsync;
  assign io_misc_hscS = vgaCtrl_io_misc_hscS;
  assign io_misc_hscC = vgaCtrl_io_misc_hscC;
  assign io_misc_hscNextS = vgaCtrl_io_misc_hscNextS;
  assign io_misc_vscS = vgaCtrl_io_misc_vscS;
  assign io_misc_vscC = vgaCtrl_io_misc_vscC;
  assign io_misc_vscNextS = vgaCtrl_io_misc_vscNextS;
  assign io_misc_fifoEmpty = vgaCtrl_io_misc_fifoEmpty;
  assign io_misc_fifoFull = vgaCtrl_io_misc_fifoFull;
  assign io_misc_nextPixelEn = vgaCtrl_io_misc_nextPixelEn;
  assign io_misc_pixelEn = vgaCtrl_io_misc_pixelEn;
  assign io_misc_nextVisib = vgaCtrl_io_misc_nextVisib;
  assign io_misc_visib = vgaCtrl_io_misc_visib;
  assign io_misc_pastVisib = vgaCtrl_io_misc_pastVisib;
  assign io_misc_drawPos_x = vgaCtrl_io_misc_drawPos_x;
  assign io_misc_drawPos_y = vgaCtrl_io_misc_drawPos_y;
  assign io_misc_pastDrawPos_x = vgaCtrl_io_misc_pastDrawPos_x;
  assign io_misc_pastDrawPos_y = vgaCtrl_io_misc_pastDrawPos_y;
  assign io_misc_size_x = vgaCtrl_io_misc_size_x;
  assign io_misc_size_y = vgaCtrl_io_misc_size_y;

endmodule

module LcvVgaGradient (
  output              io_vgaCtrlIo_en,
  output              io_vgaCtrlIo_push_valid,
  input               io_vgaCtrlIo_push_ready,
  output     [1:0]    io_vgaCtrlIo_push_payload_r,
  output     [1:0]    io_vgaCtrlIo_push_payload_g,
  output     [1:0]    io_vgaCtrlIo_push_payload_b,
  input      [1:0]    io_vgaCtrlIo_phys_col_r,
  input      [1:0]    io_vgaCtrlIo_phys_col_g,
  input      [1:0]    io_vgaCtrlIo_phys_col_b,
  input               io_vgaCtrlIo_phys_hsync,
  input               io_vgaCtrlIo_phys_vsync,
  input      [1:0]    io_vgaCtrlIo_misc_hscS,
  input      [4:0]    io_vgaCtrlIo_misc_hscC,
  input      [1:0]    io_vgaCtrlIo_misc_hscNextS,
  input      [1:0]    io_vgaCtrlIo_misc_vscS,
  input      [4:0]    io_vgaCtrlIo_misc_vscC,
  input      [1:0]    io_vgaCtrlIo_misc_vscNextS,
  input               io_vgaCtrlIo_misc_fifoEmpty,
  input               io_vgaCtrlIo_misc_fifoFull,
  input               io_vgaCtrlIo_misc_nextPixelEn,
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
  output reg          io_vidDithIo_push_valid,
  output reg [3:0]    io_vidDithIo_push_payload_r,
  output reg [3:0]    io_vidDithIo_push_payload_g,
  output reg [3:0]    io_vidDithIo_push_payload_b,
  input      [1:0]    io_vidDithIo_outp_frameCnt,
  input      [15:0]   io_vidDithIo_outp_nextPos_x,
  input      [15:0]   io_vidDithIo_outp_nextPos_y,
  input      [15:0]   io_vidDithIo_outp_pos_x,
  input      [15:0]   io_vidDithIo_outp_pos_y,
  input      [15:0]   io_vidDithIo_outp_pastPos_x,
  input      [15:0]   io_vidDithIo_outp_pastPos_y,
  input      [1:0]    io_vidDithIo_outp_col_r,
  input      [1:0]    io_vidDithIo_outp_col_g,
  input      [1:0]    io_vidDithIo_outp_col_b,
  input               clk,
  input               reset
);
  localparam LcvVgaState_front = 2'd0;
  localparam LcvVgaState_sync = 2'd1;
  localparam LcvVgaState_back = 2'd2;
  localparam LcvVgaState_visib = 2'd3;

  reg                 rCtrlPushValid;
  reg        [3:0]    rPastDithCol_r;
  reg        [3:0]    rPastDithCol_g;
  reg        [3:0]    rPastDithCol_b;
  wire                io_vgaCtrlIo_push_fire;
  wire                when_lcvVgaGradientMod_l129;
  `ifndef SYNTHESIS
  reg [39:0] io_vgaCtrlIo_misc_hscS_string;
  reg [39:0] io_vgaCtrlIo_misc_hscNextS_string;
  reg [39:0] io_vgaCtrlIo_misc_vscS_string;
  reg [39:0] io_vgaCtrlIo_misc_vscNextS_string;
  `endif


  `ifndef SYNTHESIS
  always @(*) begin
    case(io_vgaCtrlIo_misc_hscS)
      LcvVgaState_front : io_vgaCtrlIo_misc_hscS_string = "front";
      LcvVgaState_sync : io_vgaCtrlIo_misc_hscS_string = "sync ";
      LcvVgaState_back : io_vgaCtrlIo_misc_hscS_string = "back ";
      LcvVgaState_visib : io_vgaCtrlIo_misc_hscS_string = "visib";
      default : io_vgaCtrlIo_misc_hscS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_vgaCtrlIo_misc_hscNextS)
      LcvVgaState_front : io_vgaCtrlIo_misc_hscNextS_string = "front";
      LcvVgaState_sync : io_vgaCtrlIo_misc_hscNextS_string = "sync ";
      LcvVgaState_back : io_vgaCtrlIo_misc_hscNextS_string = "back ";
      LcvVgaState_visib : io_vgaCtrlIo_misc_hscNextS_string = "visib";
      default : io_vgaCtrlIo_misc_hscNextS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_vgaCtrlIo_misc_vscS)
      LcvVgaState_front : io_vgaCtrlIo_misc_vscS_string = "front";
      LcvVgaState_sync : io_vgaCtrlIo_misc_vscS_string = "sync ";
      LcvVgaState_back : io_vgaCtrlIo_misc_vscS_string = "back ";
      LcvVgaState_visib : io_vgaCtrlIo_misc_vscS_string = "visib";
      default : io_vgaCtrlIo_misc_vscS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_vgaCtrlIo_misc_vscNextS)
      LcvVgaState_front : io_vgaCtrlIo_misc_vscNextS_string = "front";
      LcvVgaState_sync : io_vgaCtrlIo_misc_vscNextS_string = "sync ";
      LcvVgaState_back : io_vgaCtrlIo_misc_vscNextS_string = "back ";
      LcvVgaState_visib : io_vgaCtrlIo_misc_vscNextS_string = "visib";
      default : io_vgaCtrlIo_misc_vscNextS_string = "?????";
    endcase
  end
  `endif

  assign io_vgaCtrlIo_en = 1'b1;
  assign io_vgaCtrlIo_push_valid = rCtrlPushValid;
  assign io_vgaCtrlIo_push_payload_r = io_vidDithIo_outp_col_r;
  assign io_vgaCtrlIo_push_payload_g = io_vidDithIo_outp_col_g;
  assign io_vgaCtrlIo_push_payload_b = io_vidDithIo_outp_col_b;
  assign io_vgaCtrlIo_push_fire = (io_vgaCtrlIo_push_valid && io_vgaCtrlIo_push_ready);
  always @(*) begin
    if(io_vgaCtrlIo_push_fire) begin
      io_vidDithIo_push_valid = 1'b1;
    end else begin
      io_vidDithIo_push_valid = 1'b0;
    end
  end

  always @(*) begin
    if(io_vgaCtrlIo_push_fire) begin
      io_vidDithIo_push_payload_r = 4'b1111;
    end else begin
      io_vidDithIo_push_payload_r = rPastDithCol_r;
    end
  end

  assign when_lcvVgaGradientMod_l129 = (io_vidDithIo_outp_pos_x == 16'h0000);
  always @(*) begin
    if(io_vgaCtrlIo_push_fire) begin
      if(when_lcvVgaGradientMod_l129) begin
        io_vidDithIo_push_payload_g = 4'b0000;
      end else begin
        io_vidDithIo_push_payload_g = (rPastDithCol_g + 4'b0001);
      end
    end else begin
      io_vidDithIo_push_payload_g = rPastDithCol_g;
    end
  end

  always @(*) begin
    if(io_vgaCtrlIo_push_fire) begin
      io_vidDithIo_push_payload_b = 4'b0000;
    end else begin
      io_vidDithIo_push_payload_b = rPastDithCol_b;
    end
  end

  always @(posedge clk or posedge reset) begin
    if(reset) begin
      rCtrlPushValid <= 1'b0;
      rPastDithCol_r <= 4'b0000;
      rPastDithCol_g <= 4'b0000;
      rPastDithCol_b <= 4'b0000;
    end else begin
      rCtrlPushValid <= io_vidDithIo_push_valid;
      rPastDithCol_r <= io_vidDithIo_push_payload_r;
      rPastDithCol_g <= io_vidDithIo_push_payload_g;
      rPastDithCol_b <= io_vidDithIo_push_payload_b;
    end
  end


endmodule

module LcvVideoDitherer (
  input               io_push_valid,
  input      [3:0]    io_push_payload_r,
  input      [3:0]    io_push_payload_g,
  input      [3:0]    io_push_payload_b,
  output     [1:0]    io_outp_frameCnt,
  output reg [15:0]   io_outp_nextPos_x,
  output reg [15:0]   io_outp_nextPos_y,
  output     [15:0]   io_outp_pos_x,
  output     [15:0]   io_outp_pos_y,
  output     [15:0]   io_outp_pastPos_x,
  output     [15:0]   io_outp_pastPos_y,
  output reg [1:0]    io_outp_col_r,
  output reg [1:0]    io_outp_col_g,
  output reg [1:0]    io_outp_col_b,
  input               clk,
  input               reset
);

  reg        [1:0]    _zz_chanDelta_1;
  wire       [0:0]    _zz_chanDelta_2;
  reg        [1:0]    _zz_chanDelta_3;
  reg        [1:0]    _zz_chanDelta_4;
  reg        [1:0]    _zz_chanDelta_5;
  reg        [1:0]    _zz_chanDelta_6;
  reg        [1:0]    _zz_chanDelta_7;
  reg        [1:0]    _zz_chanDelta_8;
  wire       [4:0]    _zz_colInPlusDelta_r;
  wire       [4:0]    _zz_colInPlusDelta_g;
  wire       [4:0]    _zz_colInPlusDelta_b;
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
  reg        [1:0]    rPastColOut_r;
  reg        [1:0]    rPastColOut_g;
  reg        [1:0]    rPastColOut_b;
  wire       [15:0]   _zz_io_outp_nextPos_x;
  wire       [15:0]   _zz_io_outp_nextPos_y;
  reg        [3:0]    dicol_r;
  reg        [3:0]    dicol_g;
  reg        [3:0]    dicol_b;
  reg        [1:0]    rOutpFrameCnt;
  reg        [15:0]   rOutpPos_x;
  reg        [15:0]   rOutpPos_y;
  reg        [15:0]   rOutpPastPos_x;
  reg        [15:0]   rOutpPastPos_y;
  wire       [0:0]    _zz_chanDelta;
  wire       [1:0]    chanDelta;
  reg        [4:0]    colInPlusDelta_r;
  reg        [4:0]    colInPlusDelta_g;
  reg        [4:0]    colInPlusDelta_b;
  wire       [4:0]    tempColInPlusDelta_r;
  wire       [4:0]    tempColInPlusDelta_g;
  wire       [4:0]    tempColInPlusDelta_b;
  wire                when_lcvVideoDithererMod_l255;
  wire                when_lcvVideoDithererMod_l265;
  wire                when_lcvVideoDithererMod_l307;
  wire                when_lcvVideoDithererMod_l318;
  wire                when_lcvVideoDithererMod_l329;

  assign _zz_colInPlusDelta_r = {3'd0, chanDelta};
  assign _zz_colInPlusDelta_g = {3'd0, chanDelta};
  assign _zz_colInPlusDelta_b = {3'd0, chanDelta};
  assign _zz_chanDelta_2 = io_outp_pos_x[0 : 0];
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
    case(io_outp_frameCnt)
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
  assign _zz_io_outp_nextPos_x = (io_outp_pos_x + 16'h0001);
  assign _zz_io_outp_nextPos_y = (io_outp_pos_y + 16'h0001);
  assign io_outp_pos_x = rOutpPos_x;
  assign io_outp_pos_y = rOutpPos_y;
  assign io_outp_pastPos_x = rOutpPastPos_x;
  assign io_outp_pastPos_y = rOutpPastPos_y;
  assign io_outp_frameCnt = rOutpFrameCnt;
  assign _zz_chanDelta = io_outp_pos_y[0 : 0];
  assign chanDelta = _zz_chanDelta_1;
  assign tempColInPlusDelta_r = {1'd0, io_push_payload_r};
  assign tempColInPlusDelta_g = {1'd0, io_push_payload_g};
  assign tempColInPlusDelta_b = {1'd0, io_push_payload_b};
  assign when_lcvVideoDithererMod_l255 = (_zz_io_outp_nextPos_x < 16'h0010);
  always @(*) begin
    if(when_lcvVideoDithererMod_l255) begin
      io_outp_nextPos_x = _zz_io_outp_nextPos_x;
    end else begin
      io_outp_nextPos_x = 16'h0000;
    end
  end

  always @(*) begin
    if(when_lcvVideoDithererMod_l255) begin
      io_outp_nextPos_y = io_outp_pos_y;
    end else begin
      if(when_lcvVideoDithererMod_l265) begin
        io_outp_nextPos_y = _zz_io_outp_nextPos_y;
      end else begin
        io_outp_nextPos_y = 16'h0000;
      end
    end
  end

  assign when_lcvVideoDithererMod_l265 = (_zz_io_outp_nextPos_y < 16'h0010);
  always @(*) begin
    if(io_push_valid) begin
      colInPlusDelta_r = (tempColInPlusDelta_r + _zz_colInPlusDelta_r);
    end else begin
      colInPlusDelta_r = 5'h00;
    end
  end

  always @(*) begin
    if(io_push_valid) begin
      colInPlusDelta_g = (tempColInPlusDelta_g + _zz_colInPlusDelta_g);
    end else begin
      colInPlusDelta_g = 5'h00;
    end
  end

  always @(*) begin
    if(io_push_valid) begin
      colInPlusDelta_b = (tempColInPlusDelta_b + _zz_colInPlusDelta_b);
    end else begin
      colInPlusDelta_b = 5'h00;
    end
  end

  assign when_lcvVideoDithererMod_l307 = colInPlusDelta_r[4];
  always @(*) begin
    if(io_push_valid) begin
      if(when_lcvVideoDithererMod_l307) begin
        dicol_r = 4'b1111;
      end else begin
        dicol_r = colInPlusDelta_r[3 : 0];
      end
    end else begin
      dicol_r = 4'b0000;
    end
  end

  assign when_lcvVideoDithererMod_l318 = colInPlusDelta_g[4];
  always @(*) begin
    if(io_push_valid) begin
      if(when_lcvVideoDithererMod_l318) begin
        dicol_g = 4'b1111;
      end else begin
        dicol_g = colInPlusDelta_g[3 : 0];
      end
    end else begin
      dicol_g = 4'b0000;
    end
  end

  assign when_lcvVideoDithererMod_l329 = colInPlusDelta_b[4];
  always @(*) begin
    if(io_push_valid) begin
      if(when_lcvVideoDithererMod_l329) begin
        dicol_b = 4'b1111;
      end else begin
        dicol_b = colInPlusDelta_b[3 : 0];
      end
    end else begin
      dicol_b = 4'b0000;
    end
  end

  always @(*) begin
    if(io_push_valid) begin
      io_outp_col_r = dicol_r[3 : 2];
    end else begin
      io_outp_col_r = rPastColOut_r;
    end
  end

  always @(*) begin
    if(io_push_valid) begin
      io_outp_col_g = dicol_g[3 : 2];
    end else begin
      io_outp_col_g = rPastColOut_g;
    end
  end

  always @(*) begin
    if(io_push_valid) begin
      io_outp_col_b = dicol_b[3 : 2];
    end else begin
      io_outp_col_b = rPastColOut_b;
    end
  end

  always @(posedge clk or posedge reset) begin
    if(reset) begin
      rOutpFrameCnt <= 2'b00;
      rOutpPos_x <= 16'h0000;
      rOutpPos_y <= 16'h0000;
      rOutpPastPos_x <= 16'h0000;
      rOutpPastPos_y <= 16'h0000;
    end else begin
      if(!when_lcvVideoDithererMod_l255) begin
        if(!when_lcvVideoDithererMod_l265) begin
          if(io_push_valid) begin
            rOutpFrameCnt <= (io_outp_frameCnt + 2'b01);
          end
        end
      end
      if(io_push_valid) begin
        rOutpPastPos_x <= rOutpPos_x;
        rOutpPastPos_y <= rOutpPos_y;
        rOutpPos_x <= io_outp_nextPos_x;
        rOutpPos_y <= io_outp_nextPos_y;
      end
    end
  end

  always @(posedge clk) begin
    if(io_push_valid) begin
      rPastColOut_r <= io_outp_col_r;
      rPastColOut_g <= io_outp_col_g;
      rPastColOut_b <= io_outp_col_b;
    end
  end


endmodule

module LcvVgaCtrl (
  input               io_en,
  input               io_push_valid,
  output              io_push_ready,
  input      [1:0]    io_push_payload_r,
  input      [1:0]    io_push_payload_g,
  input      [1:0]    io_push_payload_b,
  output     [1:0]    io_phys_col_r,
  output     [1:0]    io_phys_col_g,
  output     [1:0]    io_phys_col_b,
  output              io_phys_hsync,
  output              io_phys_vsync,
  output     [1:0]    io_misc_hscS,
  output     [4:0]    io_misc_hscC,
  output     [1:0]    io_misc_hscNextS,
  output     [1:0]    io_misc_vscS,
  output     [4:0]    io_misc_vscC,
  output     [1:0]    io_misc_vscNextS,
  output              io_misc_fifoEmpty,
  output              io_misc_fifoFull,
  output              io_misc_nextPixelEn,
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
  wire       [1:0]    fifo_io_pop_payload_r;
  wire       [1:0]    fifo_io_pop_payload_g;
  wire       [1:0]    fifo_io_pop_payload_b;
  wire                fifo_io_misc_empty;
  wire                fifo_io_misc_full;
  wire       [2:0]    _zz_clkCntP1;
  wire       [4:0]    _zz_when_lcvVgaCtrlMod_l372;
  (* keep *) wire       [1:0]    tempCol_r;
  (* keep *) wire       [1:0]    tempCol_g;
  (* keep *) wire       [1:0]    tempCol_b;
  reg        [1:0]    rPhys_col_r;
  reg        [1:0]    rPhys_col_g;
  reg        [1:0]    rPhys_col_b;
  reg                 rPhys_hsync;
  reg                 rPhys_vsync;
  reg        [1:0]    clkCnt;
  reg        [1:0]    clkCntNext;
  wire       [2:0]    clkCntP1;
  wire                when_lcvVgaCtrlMod_l282;
  wire                pixelEnNextCycle;
  reg        [1:0]    switch_lcvVgaCtrlMod_l81;
  reg        [4:0]    _zz_io_misc_hscC;
  reg        [1:0]    _zz_io_misc_hscNextS;
  reg        [1:0]    switch_lcvVgaCtrlMod_l81_1;
  reg        [4:0]    _zz_io_misc_vscC;
  reg        [1:0]    _zz_io_misc_vscNextS;
  wire       [4:0]    _zz_when_lcvVgaCtrlMod_l56;
  wire                when_lcvVgaCtrlMod_l56;
  wire       [4:0]    _zz_when_lcvVgaCtrlMod_l56_1;
  wire                when_lcvVgaCtrlMod_l56_1;
  wire       [4:0]    _zz_when_lcvVgaCtrlMod_l56_2;
  wire                when_lcvVgaCtrlMod_l56_2;
  wire       [4:0]    _zz_when_lcvVgaCtrlMod_l56_3;
  wire                when_lcvVgaCtrlMod_l56_3;
  wire                when_lcvVgaCtrlMod_l372;
  wire       [4:0]    _zz_when_lcvVgaCtrlMod_l56_4;
  wire                when_lcvVgaCtrlMod_l56_4;
  wire       [4:0]    _zz_when_lcvVgaCtrlMod_l56_5;
  wire                when_lcvVgaCtrlMod_l56_5;
  wire       [4:0]    _zz_when_lcvVgaCtrlMod_l56_6;
  wire                when_lcvVgaCtrlMod_l56_6;
  wire       [4:0]    _zz_when_lcvVgaCtrlMod_l56_7;
  wire                when_lcvVgaCtrlMod_l56_7;
  wire                when_lcvVgaCtrlMod_l412;
  reg                 rVisib;
  reg                 rPastVisib;
  reg        [15:0]   rPastDrawPos_x;
  reg        [15:0]   rPastDrawPos_y;
  `ifndef SYNTHESIS
  reg [39:0] io_misc_hscS_string;
  reg [39:0] io_misc_hscNextS_string;
  reg [39:0] io_misc_vscS_string;
  reg [39:0] io_misc_vscNextS_string;
  reg [39:0] switch_lcvVgaCtrlMod_l81_string;
  reg [39:0] _zz_io_misc_hscNextS_string;
  reg [39:0] switch_lcvVgaCtrlMod_l81_1_string;
  reg [39:0] _zz_io_misc_vscNextS_string;
  `endif


  assign _zz_clkCntP1 = {1'd0, clkCnt};
  assign _zz_when_lcvVgaCtrlMod_l372 = (_zz_io_misc_hscC + 5'h01);
  AsyncReadFifo fifo (
    .io_push_valid     (io_push_valid             ), //i
    .io_push_ready     (fifo_io_push_ready        ), //o
    .io_push_payload_r (io_push_payload_r[1:0]    ), //i
    .io_push_payload_g (io_push_payload_g[1:0]    ), //i
    .io_push_payload_b (io_push_payload_b[1:0]    ), //i
    .io_pop_valid      (fifo_io_pop_valid         ), //o
    .io_pop_ready      (fifo_io_pop_ready         ), //i
    .io_pop_payload_r  (fifo_io_pop_payload_r[1:0]), //o
    .io_pop_payload_g  (fifo_io_pop_payload_g[1:0]), //o
    .io_pop_payload_b  (fifo_io_pop_payload_b[1:0]), //o
    .io_misc_empty     (fifo_io_misc_empty        ), //o
    .io_misc_full      (fifo_io_misc_full         ), //o
    .reset             (reset                     ), //i
    .clk               (clk                       )  //i
  );
  `ifndef SYNTHESIS
  always @(*) begin
    case(io_misc_hscS)
      LcvVgaState_front : io_misc_hscS_string = "front";
      LcvVgaState_sync : io_misc_hscS_string = "sync ";
      LcvVgaState_back : io_misc_hscS_string = "back ";
      LcvVgaState_visib : io_misc_hscS_string = "visib";
      default : io_misc_hscS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_misc_hscNextS)
      LcvVgaState_front : io_misc_hscNextS_string = "front";
      LcvVgaState_sync : io_misc_hscNextS_string = "sync ";
      LcvVgaState_back : io_misc_hscNextS_string = "back ";
      LcvVgaState_visib : io_misc_hscNextS_string = "visib";
      default : io_misc_hscNextS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_misc_vscS)
      LcvVgaState_front : io_misc_vscS_string = "front";
      LcvVgaState_sync : io_misc_vscS_string = "sync ";
      LcvVgaState_back : io_misc_vscS_string = "back ";
      LcvVgaState_visib : io_misc_vscS_string = "visib";
      default : io_misc_vscS_string = "?????";
    endcase
  end
  always @(*) begin
    case(io_misc_vscNextS)
      LcvVgaState_front : io_misc_vscNextS_string = "front";
      LcvVgaState_sync : io_misc_vscNextS_string = "sync ";
      LcvVgaState_back : io_misc_vscNextS_string = "back ";
      LcvVgaState_visib : io_misc_vscNextS_string = "visib";
      default : io_misc_vscNextS_string = "?????";
    endcase
  end
  always @(*) begin
    case(switch_lcvVgaCtrlMod_l81)
      LcvVgaState_front : switch_lcvVgaCtrlMod_l81_string = "front";
      LcvVgaState_sync : switch_lcvVgaCtrlMod_l81_string = "sync ";
      LcvVgaState_back : switch_lcvVgaCtrlMod_l81_string = "back ";
      LcvVgaState_visib : switch_lcvVgaCtrlMod_l81_string = "visib";
      default : switch_lcvVgaCtrlMod_l81_string = "?????";
    endcase
  end
  always @(*) begin
    case(_zz_io_misc_hscNextS)
      LcvVgaState_front : _zz_io_misc_hscNextS_string = "front";
      LcvVgaState_sync : _zz_io_misc_hscNextS_string = "sync ";
      LcvVgaState_back : _zz_io_misc_hscNextS_string = "back ";
      LcvVgaState_visib : _zz_io_misc_hscNextS_string = "visib";
      default : _zz_io_misc_hscNextS_string = "?????";
    endcase
  end
  always @(*) begin
    case(switch_lcvVgaCtrlMod_l81_1)
      LcvVgaState_front : switch_lcvVgaCtrlMod_l81_1_string = "front";
      LcvVgaState_sync : switch_lcvVgaCtrlMod_l81_1_string = "sync ";
      LcvVgaState_back : switch_lcvVgaCtrlMod_l81_1_string = "back ";
      LcvVgaState_visib : switch_lcvVgaCtrlMod_l81_1_string = "visib";
      default : switch_lcvVgaCtrlMod_l81_1_string = "?????";
    endcase
  end
  always @(*) begin
    case(_zz_io_misc_vscNextS)
      LcvVgaState_front : _zz_io_misc_vscNextS_string = "front";
      LcvVgaState_sync : _zz_io_misc_vscNextS_string = "sync ";
      LcvVgaState_back : _zz_io_misc_vscNextS_string = "back ";
      LcvVgaState_visib : _zz_io_misc_vscNextS_string = "visib";
      default : _zz_io_misc_vscNextS_string = "?????";
    endcase
  end
  `endif

  assign io_push_ready = fifo_io_push_ready;
  assign tempCol_r = fifo_io_pop_payload_r;
  assign tempCol_g = fifo_io_pop_payload_g;
  assign tempCol_b = fifo_io_pop_payload_b;
  assign io_phys_col_r = rPhys_col_r;
  assign io_phys_col_g = rPhys_col_g;
  assign io_phys_col_b = rPhys_col_b;
  assign io_phys_hsync = rPhys_hsync;
  assign io_phys_vsync = rPhys_vsync;
  always @(*) begin
    clkCntNext = clkCnt;
    if(when_lcvVgaCtrlMod_l282) begin
      clkCntNext = clkCntP1[1 : 0];
    end else begin
      clkCntNext = 2'b00;
    end
  end

  assign clkCntP1 = (_zz_clkCntP1 + 3'b001);
  assign when_lcvVgaCtrlMod_l282 = (clkCntP1 < 3'b100);
  assign io_misc_pixelEn = (clkCnt == 2'b00);
  assign pixelEnNextCycle = (clkCntP1 == 3'b100);
  assign io_misc_nextPixelEn = (clkCntNext == 2'b00);
  assign fifo_io_pop_ready = (io_misc_nextPixelEn && io_misc_nextVisib);
  assign io_misc_fifoEmpty = fifo_io_misc_empty;
  assign io_misc_fifoFull = fifo_io_misc_full;
  always @(*) begin
    _zz_io_misc_hscNextS = switch_lcvVgaCtrlMod_l81;
    if(io_misc_pixelEn) begin
      case(switch_lcvVgaCtrlMod_l81)
        LcvVgaState_front : begin
          if(when_lcvVgaCtrlMod_l56) begin
            _zz_io_misc_hscNextS = LcvVgaState_sync;
          end
        end
        LcvVgaState_sync : begin
          if(when_lcvVgaCtrlMod_l56_1) begin
            _zz_io_misc_hscNextS = LcvVgaState_back;
          end
        end
        LcvVgaState_back : begin
          if(when_lcvVgaCtrlMod_l56_2) begin
            _zz_io_misc_hscNextS = LcvVgaState_visib;
          end
        end
        default : begin
          if(when_lcvVgaCtrlMod_l56_3) begin
            _zz_io_misc_hscNextS = LcvVgaState_front;
          end
        end
      endcase
    end else begin
      _zz_io_misc_hscNextS = switch_lcvVgaCtrlMod_l81;
    end
  end

  always @(*) begin
    _zz_io_misc_vscNextS = switch_lcvVgaCtrlMod_l81_1;
    if(io_misc_pixelEn) begin
      case(switch_lcvVgaCtrlMod_l81)
        LcvVgaState_front : begin
          _zz_io_misc_vscNextS = switch_lcvVgaCtrlMod_l81_1;
        end
        LcvVgaState_sync : begin
          _zz_io_misc_vscNextS = switch_lcvVgaCtrlMod_l81_1;
        end
        LcvVgaState_back : begin
          _zz_io_misc_vscNextS = switch_lcvVgaCtrlMod_l81_1;
        end
        default : begin
          if(when_lcvVgaCtrlMod_l372) begin
            case(switch_lcvVgaCtrlMod_l81_1)
              LcvVgaState_front : begin
                if(when_lcvVgaCtrlMod_l56_4) begin
                  _zz_io_misc_vscNextS = LcvVgaState_sync;
                end
              end
              LcvVgaState_sync : begin
                if(when_lcvVgaCtrlMod_l56_5) begin
                  _zz_io_misc_vscNextS = LcvVgaState_back;
                end
              end
              LcvVgaState_back : begin
                if(when_lcvVgaCtrlMod_l56_6) begin
                  _zz_io_misc_vscNextS = LcvVgaState_visib;
                end
              end
              default : begin
                if(when_lcvVgaCtrlMod_l56_7) begin
                  _zz_io_misc_vscNextS = LcvVgaState_front;
                end
              end
            endcase
          end else begin
            _zz_io_misc_vscNextS = switch_lcvVgaCtrlMod_l81_1;
          end
        end
      endcase
    end else begin
      _zz_io_misc_vscNextS = switch_lcvVgaCtrlMod_l81_1;
    end
  end

  assign io_misc_hscS = switch_lcvVgaCtrlMod_l81;
  assign io_misc_hscC = _zz_io_misc_hscC;
  assign io_misc_hscNextS = _zz_io_misc_hscNextS;
  assign io_misc_vscS = switch_lcvVgaCtrlMod_l81_1;
  assign io_misc_vscC = _zz_io_misc_vscC;
  assign io_misc_vscNextS = _zz_io_misc_vscNextS;
  assign _zz_when_lcvVgaCtrlMod_l56 = (_zz_io_misc_hscC + 5'h01);
  assign when_lcvVgaCtrlMod_l56 = (5'h01 <= _zz_when_lcvVgaCtrlMod_l56);
  assign _zz_when_lcvVgaCtrlMod_l56_1 = (_zz_io_misc_hscC + 5'h01);
  assign when_lcvVgaCtrlMod_l56_1 = (5'h01 <= _zz_when_lcvVgaCtrlMod_l56_1);
  assign _zz_when_lcvVgaCtrlMod_l56_2 = (_zz_io_misc_hscC + 5'h01);
  assign when_lcvVgaCtrlMod_l56_2 = (5'h01 <= _zz_when_lcvVgaCtrlMod_l56_2);
  assign _zz_when_lcvVgaCtrlMod_l56_3 = (_zz_io_misc_hscC + 5'h01);
  assign when_lcvVgaCtrlMod_l56_3 = (5'h10 <= _zz_when_lcvVgaCtrlMod_l56_3);
  assign when_lcvVgaCtrlMod_l372 = (5'h10 <= _zz_when_lcvVgaCtrlMod_l372);
  assign _zz_when_lcvVgaCtrlMod_l56_4 = (_zz_io_misc_vscC + 5'h01);
  assign when_lcvVgaCtrlMod_l56_4 = (5'h01 <= _zz_when_lcvVgaCtrlMod_l56_4);
  assign _zz_when_lcvVgaCtrlMod_l56_5 = (_zz_io_misc_vscC + 5'h01);
  assign when_lcvVgaCtrlMod_l56_5 = (5'h01 <= _zz_when_lcvVgaCtrlMod_l56_5);
  assign _zz_when_lcvVgaCtrlMod_l56_6 = (_zz_io_misc_vscC + 5'h01);
  assign when_lcvVgaCtrlMod_l56_6 = (5'h01 <= _zz_when_lcvVgaCtrlMod_l56_6);
  assign _zz_when_lcvVgaCtrlMod_l56_7 = (_zz_io_misc_vscC + 5'h01);
  assign when_lcvVgaCtrlMod_l56_7 = (5'h10 <= _zz_when_lcvVgaCtrlMod_l56_7);
  assign when_lcvVgaCtrlMod_l412 = (! io_en);
  assign io_misc_drawPos_x = {11'd0, _zz_io_misc_hscC};
  assign io_misc_drawPos_y = {11'd0, _zz_io_misc_vscC};
  assign io_misc_size_x = 16'h0010;
  assign io_misc_size_y = 16'h0010;
  assign io_misc_nextVisib = ((_zz_io_misc_hscNextS == LcvVgaState_visib) && (_zz_io_misc_vscNextS == LcvVgaState_visib));
  assign io_misc_visib = rVisib;
  assign io_misc_pastVisib = rPastVisib;
  assign io_misc_pastDrawPos_x = rPastDrawPos_x;
  assign io_misc_pastDrawPos_y = rPastDrawPos_y;
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      rPhys_col_r <= 2'b00;
      rPhys_col_g <= 2'b00;
      rPhys_col_b <= 2'b00;
      rPhys_hsync <= 1'b0;
      rPhys_vsync <= 1'b0;
      clkCnt <= 2'b00;
      switch_lcvVgaCtrlMod_l81 <= LcvVgaState_front;
      _zz_io_misc_hscC <= 5'h00;
      switch_lcvVgaCtrlMod_l81_1 <= LcvVgaState_front;
      _zz_io_misc_vscC <= 5'h00;
      rVisib <= 1'b0;
      rPastVisib <= 1'b0;
      rPastDrawPos_x <= 16'h0000;
      rPastDrawPos_y <= 16'h0000;
    end else begin
      clkCnt <= clkCntNext;
      switch_lcvVgaCtrlMod_l81 <= _zz_io_misc_hscNextS;
      switch_lcvVgaCtrlMod_l81_1 <= _zz_io_misc_vscNextS;
      if(io_misc_pixelEn) begin
        case(switch_lcvVgaCtrlMod_l81)
          LcvVgaState_front : begin
            if(when_lcvVgaCtrlMod_l56) begin
              _zz_io_misc_hscC <= 5'h00;
            end else begin
              _zz_io_misc_hscC <= _zz_when_lcvVgaCtrlMod_l56;
            end
          end
          LcvVgaState_sync : begin
            if(when_lcvVgaCtrlMod_l56_1) begin
              _zz_io_misc_hscC <= 5'h00;
            end else begin
              _zz_io_misc_hscC <= _zz_when_lcvVgaCtrlMod_l56_1;
            end
          end
          LcvVgaState_back : begin
            if(when_lcvVgaCtrlMod_l56_2) begin
              _zz_io_misc_hscC <= 5'h00;
            end else begin
              _zz_io_misc_hscC <= _zz_when_lcvVgaCtrlMod_l56_2;
            end
          end
          default : begin
            if(when_lcvVgaCtrlMod_l56_3) begin
              _zz_io_misc_hscC <= 5'h00;
            end else begin
              _zz_io_misc_hscC <= _zz_when_lcvVgaCtrlMod_l56_3;
            end
          end
        endcase
        case(switch_lcvVgaCtrlMod_l81)
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
            if(when_lcvVgaCtrlMod_l372) begin
              case(switch_lcvVgaCtrlMod_l81_1)
                LcvVgaState_front : begin
                  if(when_lcvVgaCtrlMod_l56_4) begin
                    _zz_io_misc_vscC <= 5'h00;
                  end else begin
                    _zz_io_misc_vscC <= _zz_when_lcvVgaCtrlMod_l56_4;
                  end
                end
                LcvVgaState_sync : begin
                  if(when_lcvVgaCtrlMod_l56_5) begin
                    _zz_io_misc_vscC <= 5'h00;
                  end else begin
                    _zz_io_misc_vscC <= _zz_when_lcvVgaCtrlMod_l56_5;
                  end
                end
                LcvVgaState_back : begin
                  if(when_lcvVgaCtrlMod_l56_6) begin
                    _zz_io_misc_vscC <= 5'h00;
                  end else begin
                    _zz_io_misc_vscC <= _zz_when_lcvVgaCtrlMod_l56_6;
                  end
                end
                default : begin
                  if(when_lcvVgaCtrlMod_l56_7) begin
                    _zz_io_misc_vscC <= 5'h00;
                  end else begin
                    _zz_io_misc_vscC <= _zz_when_lcvVgaCtrlMod_l56_7;
                  end
                end
              endcase
            end
          end
        endcase
        case(switch_lcvVgaCtrlMod_l81_1)
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
          if(when_lcvVgaCtrlMod_l412) begin
            rPhys_col_r <= 2'b11;
            rPhys_col_g <= 2'b11;
            rPhys_col_b <= 2'b11;
          end else begin
            rPhys_col_r <= tempCol_r;
            rPhys_col_g <= tempCol_g;
            rPhys_col_b <= tempCol_b;
          end
        end else begin
          rPhys_col_r <= 2'b00;
          rPhys_col_g <= 2'b00;
          rPhys_col_b <= 2'b00;
        end
      end
      rVisib <= io_misc_nextVisib;
      rPastVisib <= io_misc_visib;
      rPastDrawPos_x <= io_misc_drawPos_x;
      rPastDrawPos_y <= io_misc_drawPos_y;
    end
  end


endmodule

module AsyncReadFifo (
  input               io_push_valid,
  output              io_push_ready,
  input      [1:0]    io_push_payload_r,
  input      [1:0]    io_push_payload_g,
  input      [1:0]    io_push_payload_b,
  output              io_pop_valid,
  input               io_pop_ready,
  output     [1:0]    io_pop_payload_r,
  output     [1:0]    io_pop_payload_g,
  output     [1:0]    io_pop_payload_b,
  output              io_misc_empty,
  output              io_misc_full,
  input               reset,
  input               clk
);

  wire                sbPush_io_misc_clear;
  wire       [1:0]    sbPop_io_prev_payload_r;
  wire       [1:0]    sbPop_io_prev_payload_g;
  wire       [1:0]    sbPop_io_prev_payload_b;
  wire                sbPop_io_misc_clear;
  wire       [5:0]    _zz_loc_arr_port0;
  wire                sbPush_io_next_valid;
  wire       [1:0]    sbPush_io_next_payload_r;
  wire       [1:0]    sbPush_io_next_payload_g;
  wire       [1:0]    sbPush_io_next_payload_b;
  wire                sbPush_io_prev_ready;
  wire                sbPop_io_next_valid;
  wire       [1:0]    sbPop_io_next_payload_r;
  wire       [1:0]    sbPop_io_next_payload_g;
  wire       [1:0]    sbPop_io_next_payload_b;
  wire                sbPop_io_prev_ready;
  wire       [4:0]    _zz_loc_tailPlus1;
  wire       [4:0]    _zz_loc_headPlus1;
  wire       [4:0]    _zz_loc_nextHeadPlus1;
  wire       [4:0]    _zz_loc_nextEmpty;
  wire       [4:0]    _zz_loc_nextFull;
  wire       [3:0]    _zz_locFormal_testHead;
  wire       [5:0]    _zz_loc_arr_port;
  reg                 _zz_1;
  wire                wrEn;
  wire                rdValid;
  wire       [1:0]    rdDataPrev_r;
  wire       [1:0]    rdDataPrev_g;
  wire       [1:0]    rdDataPrev_b;
  (* keep *) wire                rdEn;
  wire                fifo_sbPop_io_next_fire;
  reg                 loc_empty;
  reg                 loc_full;
  wire       [4:0]    loc_uintDepth;
  reg        [3:0]    loc_head;
  reg        [3:0]    loc_tail;
  reg        [3:0]    loc_nextHead;
  reg        [3:0]    loc_nextTail;
  (* keep *) wire       [4:0]    loc_tempTailPlus1;
  wire       [4:0]    loc_tailPlus1;
  (* keep *) wire       [4:0]    loc_tempHeadPlus1;
  wire       [4:0]    loc_headPlus1;
  (* keep *) wire       [4:0]    loc_tempNextHeadPlus1;
  wire       [4:0]    loc_nextHeadPlus1;
  (* keep *) wire                loc_tempOorNextHeadPlus1;
  wire                loc_oorNextHeadPlus1;
  (* keep *) reg        [4:0]    loc_tempIncrNextHead;
  wire                loc_nextEmpty;
  wire                loc_nextFull;
  (* keep *) wire       [1:0]    locFormal_lastTailVal_r;
  (* keep *) wire       [1:0]    locFormal_lastTailVal_g;
  (* keep *) wire       [1:0]    locFormal_lastTailVal_b;
  wire       [3:0]    locFormal_testHead;
  wire       [5:0]    _zz_io_prev_payload_r;
  wire                when_fifoMods_l640;
  wire                when_fifoMods_l643;
  wire                when_fifoMods_l657;
  wire                when_fifoMods_l658;
  wire                when_fifoMods_l734;
  wire                when_fifoMods_l738;
  (* ram_style = "ultra" , keep *) reg [5:0] loc_arr [0:15];

  assign _zz_loc_tailPlus1 = {1'd0, loc_tail};
  assign _zz_loc_headPlus1 = {1'd0, loc_head};
  assign _zz_loc_nextHeadPlus1 = {1'd0, loc_nextHead};
  assign _zz_loc_nextEmpty = {1'd0, loc_nextTail};
  assign _zz_loc_nextFull = {1'd0, loc_nextTail};
  assign _zz_locFormal_testHead = (loc_head + 4'b0001);
  assign _zz_loc_arr_port = {io_push_payload_b,{io_push_payload_g,io_push_payload_r}};
  assign _zz_loc_arr_port0 = loc_arr[loc_tail];
  always @(posedge clk) begin
    if(_zz_1) begin
      loc_arr[loc_head] <= _zz_loc_arr_port;
    end
  end

  PipeSkidBuf sbPush (
    .io_next_valid     (sbPush_io_next_valid         ), //o
    .io_next_ready     (1'b1                         ), //i
    .io_next_payload_r (sbPush_io_next_payload_r[1:0]), //o
    .io_next_payload_g (sbPush_io_next_payload_g[1:0]), //o
    .io_next_payload_b (sbPush_io_next_payload_b[1:0]), //o
    .io_prev_valid     (io_push_valid                ), //i
    .io_prev_ready     (sbPush_io_prev_ready         ), //o
    .io_prev_payload_r (io_push_payload_r[1:0]       ), //i
    .io_prev_payload_g (io_push_payload_g[1:0]       ), //i
    .io_prev_payload_b (io_push_payload_b[1:0]       ), //i
    .io_misc_busy      (io_misc_full                 ), //i
    .io_misc_clear     (sbPush_io_misc_clear         ), //i
    .reset             (reset                        ), //i
    .clk               (clk                          )  //i
  );
  PipeSkidBuf sbPop (
    .io_next_valid     (sbPop_io_next_valid         ), //o
    .io_next_ready     (io_pop_ready                ), //i
    .io_next_payload_r (sbPop_io_next_payload_r[1:0]), //o
    .io_next_payload_g (sbPop_io_next_payload_g[1:0]), //o
    .io_next_payload_b (sbPop_io_next_payload_b[1:0]), //o
    .io_prev_valid     (rdValid                     ), //i
    .io_prev_ready     (sbPop_io_prev_ready         ), //o
    .io_prev_payload_r (sbPop_io_prev_payload_r[1:0]), //i
    .io_prev_payload_g (sbPop_io_prev_payload_g[1:0]), //i
    .io_prev_payload_b (sbPop_io_prev_payload_b[1:0]), //i
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
  assign rdDataPrev_r = 2'b00;
  assign rdDataPrev_g = 2'b00;
  assign rdDataPrev_b = 2'b00;
  assign fifo_sbPop_io_next_fire = (sbPop_io_next_valid && io_pop_ready);
  assign rdEn = fifo_sbPop_io_next_fire;
  assign loc_uintDepth = 5'h10;
  assign loc_tailPlus1 = (_zz_loc_tailPlus1 + 5'h01);
  assign loc_tempTailPlus1 = loc_tailPlus1;
  assign loc_headPlus1 = (_zz_loc_headPlus1 + 5'h01);
  assign loc_tempHeadPlus1 = loc_headPlus1;
  assign loc_nextHeadPlus1 = (_zz_loc_nextHeadPlus1 + 5'h01);
  assign loc_tempNextHeadPlus1 = loc_nextHeadPlus1;
  assign loc_oorNextHeadPlus1 = (loc_uintDepth <= loc_nextHeadPlus1);
  assign loc_tempOorNextHeadPlus1 = loc_oorNextHeadPlus1;
  always @(*) begin
    if(loc_oorNextHeadPlus1) begin
      loc_tempIncrNextHead = 5'h00;
    end else begin
      loc_tempIncrNextHead = loc_nextHeadPlus1;
    end
  end

  assign loc_nextEmpty = ((loc_tempIncrNextHead != _zz_loc_nextEmpty) && (loc_nextHead == loc_nextTail));
  assign loc_nextFull = (loc_tempIncrNextHead == _zz_loc_nextFull);
  assign locFormal_lastTailVal_r = 2'b00;
  assign locFormal_lastTailVal_g = 2'b00;
  assign locFormal_lastTailVal_b = 2'b00;
  assign locFormal_testHead = (_zz_locFormal_testHead % 5'h10);
  assign _zz_io_prev_payload_r = _zz_loc_arr_port0;
  assign sbPop_io_prev_payload_r = _zz_io_prev_payload_r[1 : 0];
  assign sbPop_io_prev_payload_g = _zz_io_prev_payload_r[3 : 2];
  assign sbPop_io_prev_payload_b = _zz_io_prev_payload_r[5 : 4];
  assign rdValid = 1'b1;
  assign io_misc_empty = loc_empty;
  assign io_misc_full = loc_full;
  always @(*) begin
    if(reset) begin
      loc_nextHead = 4'b0000;
    end else begin
      if(when_fifoMods_l640) begin
        if(when_fifoMods_l643) begin
          loc_nextHead = 4'b0000;
        end else begin
          loc_nextHead = loc_headPlus1[3:0];
        end
      end else begin
        loc_nextHead = loc_head;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      loc_nextTail = 4'b0000;
    end else begin
      if(when_fifoMods_l657) begin
        if(when_fifoMods_l658) begin
          loc_nextTail = 4'b0000;
        end else begin
          loc_nextTail = loc_tailPlus1[3:0];
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
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      loc_empty <= 1'b1;
      loc_full <= 1'b0;
      loc_head <= 4'b0000;
      loc_tail <= 4'b0000;
    end else begin
      loc_head <= loc_nextHead;
      loc_tail <= loc_nextTail;
      loc_empty <= loc_nextEmpty;
      loc_full <= loc_nextFull;
    end
  end


endmodule

//PipeSkidBuf_1 replaced by PipeSkidBuf

module PipeSkidBuf (
  (* keep *) output              io_next_valid,
  (* keep *) input               io_next_ready,
  (* keep *) output reg [1:0]    io_next_payload_r,
  (* keep *) output reg [1:0]    io_next_payload_g,
  (* keep *) output reg [1:0]    io_next_payload_b,
  (* keep *) input               io_prev_valid,
  (* keep *) output              io_prev_ready,
  (* keep *) input      [1:0]    io_prev_payload_r,
  (* keep *) input      [1:0]    io_prev_payload_g,
  (* keep *) input      [1:0]    io_prev_payload_b,
  (* keep *) input               io_misc_busy,
  (* keep *) input               io_misc_clear,
  input               reset,
  input               clk
);

  wire                _zz_io_prev_ready;
  (* keep *) wire                _zz_when_Stream_l369;
  (* keep *) wire                _zz_io_prev_ready_1;
  (* keep *) wire       [1:0]    _zz_io_next_payload_r;
  (* keep *) wire       [1:0]    _zz_io_next_payload_g;
  (* keep *) wire       [1:0]    _zz_io_next_payload_b;
  (* keep *) wire                _zz_io_next_valid;
  (* keep *) wire                _zz_1;
  (* keep *) wire       [1:0]    _zz_io_next_payload_r_1;
  (* keep *) wire       [1:0]    _zz_io_next_payload_g_1;
  (* keep *) wire       [1:0]    _zz_io_next_payload_b_1;
  reg                 _zz_2;
  reg                 _zz_io_prev_ready_2;
  reg        [1:0]    _zz_io_next_payload_r_2;
  reg        [1:0]    _zz_io_next_payload_g_2;
  reg        [1:0]    _zz_io_next_payload_b_2;
  wire                _zz_when_Stream_l369_1;
  reg                 _zz_when_Stream_l369_2;
  reg        [1:0]    _zz_io_next_payload_r_3;
  reg        [1:0]    _zz_io_next_payload_g_3;
  reg        [1:0]    _zz_io_next_payload_b_3;
  wire                when_Stream_l369;
  wire                io_prev_fire;

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
      io_next_payload_r = 2'b00;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_r = _zz_io_next_payload_r_1;
      end else begin
        io_next_payload_r = 2'b00;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      io_next_payload_g = 2'b00;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_g = _zz_io_next_payload_g_1;
      end else begin
        io_next_payload_g = 2'b00;
      end
    end
  end

  always @(*) begin
    if(reset) begin
      io_next_payload_b = 2'b00;
    end else begin
      if(io_prev_fire) begin
        io_next_payload_b = _zz_io_next_payload_b_1;
      end else begin
        io_next_payload_b = 2'b00;
      end
    end
  end

  assign io_prev_fire = (io_prev_valid && io_prev_ready);
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      _zz_io_prev_ready_2 <= 1'b1;
      _zz_when_Stream_l369_2 <= 1'b0;
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


endmodule
