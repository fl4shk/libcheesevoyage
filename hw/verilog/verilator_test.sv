`timescale 1ns/1ps

interface Gpu2dIo () ;

  Gpu2dPushInp    push();
  logic           pop_valid ;
  logic           pop_ready ;
  logic           pop_payload_ctrlEn ;
  logic  [3:0]    pop_payload_col_r ;
  logic  [3:0]    pop_payload_col_g ;
  logic  [3:0]    pop_payload_col_b ;
  logic           pop_payload_physPosInfo_posWillOverflow_x ;
  logic           pop_payload_physPosInfo_posWillOverflow_y ;
  logic  [9:0]    pop_payload_physPosInfo_nextPos_x ;
  logic  [8:0]    pop_payload_physPosInfo_nextPos_y ;
  logic  [9:0]    pop_payload_physPosInfo_pos_x ;
  logic  [8:0]    pop_payload_physPosInfo_pos_y ;
  logic  [9:0]    pop_payload_physPosInfo_pastPos_x ;
  logic  [8:0]    pop_payload_physPosInfo_pastPos_y ;
  logic           pop_payload_physPosInfo_changingRow ;

  //modport slv (
  //  .push(push.mst),
  //  input           pop_valid,
  //  output          pop_ready,
  //  input           pop_payload_ctrlEn,
  //  input           pop_payload_col_r,
  //  input           pop_payload_col_g,
  //  input           pop_payload_col_b,
  //  input           pop_payload_physPosInfo_posWillOverflow_x,
  //  input           pop_payload_physPosInfo_posWillOverflow_y,
  //  input           pop_payload_physPosInfo_nextPos_x,
  //  input           pop_payload_physPosInfo_nextPos_y,
  //  input           pop_payload_physPosInfo_pos_x,
  //  input           pop_payload_physPosInfo_pos_y,
  //  input           pop_payload_physPosInfo_pastPos_x,
  //  input           pop_payload_physPosInfo_pastPos_y,
  //  input           pop_payload_physPosInfo_changingRow
  //);

  //modport mst (
  //  .push(push.slv),
  //  output          pop_valid,
  //  input           pop_ready,
  //  output          pop_payload_ctrlEn,
  //  output          pop_payload_col_r,
  //  output          pop_payload_col_g,
  //  output          pop_payload_col_b,
  //  output          pop_payload_physPosInfo_posWillOverflow_x,
  //  output          pop_payload_physPosInfo_posWillOverflow_y,
  //  output          pop_payload_physPosInfo_nextPos_x,
  //  output          pop_payload_physPosInfo_nextPos_y,
  //  output          pop_payload_physPosInfo_pos_x,
  //  output          pop_payload_physPosInfo_pos_y,
  //  output          pop_payload_physPosInfo_pastPos_x,
  //  output          pop_payload_physPosInfo_pastPos_y,
  //  output          pop_payload_physPosInfo_changingRow
  //);

endinterface

interface Gpu2dPushInp () ;

  logic           colorMathTilePush_valid ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_0 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_1 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_2 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_3 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_4 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_5 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_6 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_7 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_8 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_9 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_10 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_11 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_12 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_13 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_14 ;
  logic  [5:0]    colorMathTilePush_payload_tileSlice_colIdxVec_15 ;
  logic  [8:0]    colorMathTilePush_payload_memIdx ;
  logic           colorMathEntryPush_valid ;
  logic  [4:0]    colorMathEntryPush_payload_bgEntry_tileIdx ;
  logic           colorMathEntryPush_payload_bgEntry_dispFlip_x ;
  logic           colorMathEntryPush_payload_bgEntry_dispFlip_y ;
  logic  [8:0]    colorMathEntryPush_payload_memIdx ;
  logic           colorMathAttrsPush_valid ;
  logic  [8:0]    colorMathAttrsPush_payload_bgAttrs_scroll_x ;
  logic  [7:0]    colorMathAttrsPush_payload_bgAttrs_scroll_y ;
  logic           colorMathAttrsPush_payload_bgAttrs_fbAttrs_doIt ;
  logic  [0:0]    colorMathAttrsPush_payload_bgAttrs_fbAttrs_tileMemBaseAddr ;
  logic           colorMathPalEntryPush_valid ;
  logic  [3:0]    colorMathPalEntryPush_payload_bgPalEntry_col_r ;
  logic  [3:0]    colorMathPalEntryPush_payload_bgPalEntry_col_g ;
  logic  [3:0]    colorMathPalEntryPush_payload_bgPalEntry_col_b ;
  logic  [5:0]    colorMathPalEntryPush_payload_memIdx ;
  logic           bgTilePush_valid ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_0 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_1 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_2 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_3 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_4 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_5 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_6 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_7 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_8 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_9 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_10 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_11 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_12 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_13 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_14 ;
  logic  [5:0]    bgTilePush_payload_tileSlice_colIdxVec_15 ;
  logic  [11:0]   bgTilePush_payload_memIdx ;
  logic           bgEntryPushVec_0_valid ;
  logic  [7:0]    bgEntryPushVec_0_payload_bgEntry_tileIdx ;
  logic           bgEntryPushVec_0_payload_bgEntry_dispFlip_x ;
  logic           bgEntryPushVec_0_payload_bgEntry_dispFlip_y ;
  logic  [8:0]    bgEntryPushVec_0_payload_memIdx ;
  logic           bgEntryPushVec_1_valid ;
  logic  [7:0]    bgEntryPushVec_1_payload_bgEntry_tileIdx ;
  logic           bgEntryPushVec_1_payload_bgEntry_dispFlip_x ;
  logic           bgEntryPushVec_1_payload_bgEntry_dispFlip_y ;
  logic  [8:0]    bgEntryPushVec_1_payload_memIdx ;
  logic           bgAttrsPushVec_0_valid ;
  logic  [8:0]    bgAttrsPushVec_0_payload_bgAttrs_scroll_x ;
  logic  [7:0]    bgAttrsPushVec_0_payload_bgAttrs_scroll_y ;
  logic           bgAttrsPushVec_0_payload_bgAttrs_fbAttrs_doIt ;
  logic  [0:0]    bgAttrsPushVec_0_payload_bgAttrs_fbAttrs_tileMemBaseAddr ;
  logic           bgAttrsPushVec_1_valid ;
  logic  [8:0]    bgAttrsPushVec_1_payload_bgAttrs_scroll_x ;
  logic  [7:0]    bgAttrsPushVec_1_payload_bgAttrs_scroll_y ;
  logic           bgAttrsPushVec_1_payload_bgAttrs_fbAttrs_doIt ;
  logic  [0:0]    bgAttrsPushVec_1_payload_bgAttrs_fbAttrs_tileMemBaseAddr ;
  logic           bgPalEntryPush_valid ;
  logic  [3:0]    bgPalEntryPush_payload_bgPalEntry_col_r ;
  logic  [3:0]    bgPalEntryPush_payload_bgPalEntry_col_g ;
  logic  [3:0]    bgPalEntryPush_payload_bgPalEntry_col_b ;
  logic  [5:0]    bgPalEntryPush_payload_memIdx ;
  logic           objTilePush_valid ;
  logic           objTilePush_ready ;
  logic  [5:0]    objTilePush_payload_tileSlice_colIdxVec_0 ;
  logic  [5:0]    objTilePush_payload_tileSlice_colIdxVec_1 ;
  logic  [5:0]    objTilePush_payload_tileSlice_colIdxVec_2 ;
  logic  [5:0]    objTilePush_payload_tileSlice_colIdxVec_3 ;
  logic  [12:0]   objTilePush_payload_memIdx ;
  logic           objAffineTilePush_valid ;
  logic  [5:0]    objAffineTilePush_payload_tilePx ;
  logic  [11:0]   objAffineTilePush_payload_memIdx ;
  logic           objAttrsPush_valid ;
  logic  [6:0]    objAttrsPush_payload_objAttrs_tileIdx ;
  logic  [10:0]   objAttrsPush_payload_objAttrs_pos_x ;
  logic  [9:0]    objAttrsPush_payload_objAttrs_pos_y ;
  logic  [0:0]    objAttrsPush_payload_objAttrs_prio ;
  logic  [4:0]    objAttrsPush_payload_objAttrs_size2d_x ;
  logic  [4:0]    objAttrsPush_payload_objAttrs_size2d_y ;
  logic           objAttrsPush_payload_objAttrs_dispFlip_x ;
  logic           objAttrsPush_payload_objAttrs_dispFlip_y ;
  logic  [2:0]    objAttrsPush_payload_memIdx ;
  logic           objAffineAttrsPush_valid ;
  logic  [3:0]    objAffineAttrsPush_payload_objAttrs_tileIdx ;
  logic  [10:0]   objAffineAttrsPush_payload_objAttrs_pos_x ;
  logic  [9:0]    objAffineAttrsPush_payload_objAttrs_pos_y ;
  logic  [0:0]    objAffineAttrsPush_payload_objAttrs_prio ;
  logic           objAffineAttrsPush_payload_objAttrs_affine_doIt ;
  logic  [15:0]   objAffineAttrsPush_payload_objAttrs_affine_mat_0_0 ;
  logic  [15:0]   objAffineAttrsPush_payload_objAttrs_affine_mat_0_1 ;
  logic  [15:0]   objAffineAttrsPush_payload_objAttrs_affine_mat_1_0 ;
  logic  [15:0]   objAffineAttrsPush_payload_objAttrs_affine_mat_1_1 ;
  logic  [5:0]    objAffineAttrsPush_payload_objAttrs_size2d_x ;
  logic  [5:0]    objAffineAttrsPush_payload_objAttrs_size2d_y ;
  logic           objAffineAttrsPush_payload_objAttrs_dispFlip_x ;
  logic           objAffineAttrsPush_payload_objAttrs_dispFlip_y ;
  logic  [3:0]    objAffineAttrsPush_payload_memIdx ;
  logic           objPalEntryPush_valid ;
  logic  [3:0]    objPalEntryPush_payload_objPalEntry_col_r ;
  logic  [3:0]    objPalEntryPush_payload_objPalEntry_col_g ;
  logic  [3:0]    objPalEntryPush_payload_objPalEntry_col_b ;
  logic  [5:0]    objPalEntryPush_payload_memIdx ;

  //modport slv (
  //  input           colorMathTilePush_valid,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_0,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_1,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_2,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_3,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_4,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_5,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_6,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_7,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_8,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_9,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_10,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_11,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_12,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_13,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_14,
  //  input           colorMathTilePush_payload_tileSlice_colIdxVec_15,
  //  input           colorMathTilePush_payload_memIdx,
  //  input           colorMathEntryPush_valid,
  //  input           colorMathEntryPush_payload_bgEntry_tileIdx,
  //  input           colorMathEntryPush_payload_bgEntry_dispFlip_x,
  //  input           colorMathEntryPush_payload_bgEntry_dispFlip_y,
  //  input           colorMathEntryPush_payload_memIdx,
  //  input           colorMathAttrsPush_valid,
  //  input           colorMathAttrsPush_payload_bgAttrs_scroll_x,
  //  input           colorMathAttrsPush_payload_bgAttrs_scroll_y,
  //  input           colorMathAttrsPush_payload_bgAttrs_fbAttrs_doIt,
  //  input           colorMathAttrsPush_payload_bgAttrs_fbAttrs_tileMemBaseAddr,
  //  input           colorMathPalEntryPush_valid,
  //  input           colorMathPalEntryPush_payload_bgPalEntry_col_r,
  //  input           colorMathPalEntryPush_payload_bgPalEntry_col_g,
  //  input           colorMathPalEntryPush_payload_bgPalEntry_col_b,
  //  input           colorMathPalEntryPush_payload_memIdx,
  //  input           bgTilePush_valid,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_0,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_1,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_2,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_3,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_4,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_5,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_6,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_7,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_8,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_9,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_10,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_11,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_12,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_13,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_14,
  //  input           bgTilePush_payload_tileSlice_colIdxVec_15,
  //  input           bgTilePush_payload_memIdx,
  //  input           bgEntryPushVec_0_valid,
  //  input           bgEntryPushVec_0_payload_bgEntry_tileIdx,
  //  input           bgEntryPushVec_0_payload_bgEntry_dispFlip_x,
  //  input           bgEntryPushVec_0_payload_bgEntry_dispFlip_y,
  //  input           bgEntryPushVec_0_payload_memIdx,
  //  input           bgEntryPushVec_1_valid,
  //  input           bgEntryPushVec_1_payload_bgEntry_tileIdx,
  //  input           bgEntryPushVec_1_payload_bgEntry_dispFlip_x,
  //  input           bgEntryPushVec_1_payload_bgEntry_dispFlip_y,
  //  input           bgEntryPushVec_1_payload_memIdx,
  //  input           bgAttrsPushVec_0_valid,
  //  input           bgAttrsPushVec_0_payload_bgAttrs_scroll_x,
  //  input           bgAttrsPushVec_0_payload_bgAttrs_scroll_y,
  //  input           bgAttrsPushVec_0_payload_bgAttrs_fbAttrs_doIt,
  //  input           bgAttrsPushVec_0_payload_bgAttrs_fbAttrs_tileMemBaseAddr,
  //  input           bgAttrsPushVec_1_valid,
  //  input           bgAttrsPushVec_1_payload_bgAttrs_scroll_x,
  //  input           bgAttrsPushVec_1_payload_bgAttrs_scroll_y,
  //  input           bgAttrsPushVec_1_payload_bgAttrs_fbAttrs_doIt,
  //  input           bgAttrsPushVec_1_payload_bgAttrs_fbAttrs_tileMemBaseAddr,
  //  input           bgPalEntryPush_valid,
  //  input           bgPalEntryPush_payload_bgPalEntry_col_r,
  //  input           bgPalEntryPush_payload_bgPalEntry_col_g,
  //  input           bgPalEntryPush_payload_bgPalEntry_col_b,
  //  input           bgPalEntryPush_payload_memIdx,
  //  input           objTilePush_valid,
  //  output          objTilePush_ready,
  //  input           objTilePush_payload_tileSlice_colIdxVec_0,
  //  input           objTilePush_payload_tileSlice_colIdxVec_1,
  //  input           objTilePush_payload_tileSlice_colIdxVec_2,
  //  input           objTilePush_payload_tileSlice_colIdxVec_3,
  //  input           objTilePush_payload_memIdx,
  //  input           objAffineTilePush_valid,
  //  input           objAffineTilePush_payload_tilePx,
  //  input           objAffineTilePush_payload_memIdx,
  //  input           objAttrsPush_valid,
  //  input           objAttrsPush_payload_objAttrs_tileIdx,
  //  input           objAttrsPush_payload_objAttrs_pos_x,
  //  input           objAttrsPush_payload_objAttrs_pos_y,
  //  input           objAttrsPush_payload_objAttrs_prio,
  //  input           objAttrsPush_payload_objAttrs_size2d_x,
  //  input           objAttrsPush_payload_objAttrs_size2d_y,
  //  input           objAttrsPush_payload_objAttrs_dispFlip_x,
  //  input           objAttrsPush_payload_objAttrs_dispFlip_y,
  //  input           objAttrsPush_payload_memIdx,
  //  input           objAffineAttrsPush_valid,
  //  input           objAffineAttrsPush_payload_objAttrs_tileIdx,
  //  input           objAffineAttrsPush_payload_objAttrs_pos_x,
  //  input           objAffineAttrsPush_payload_objAttrs_pos_y,
  //  input           objAffineAttrsPush_payload_objAttrs_prio,
  //  input           objAffineAttrsPush_payload_objAttrs_affine_doIt,
  //  input           objAffineAttrsPush_payload_objAttrs_affine_mat_0_0,
  //  input           objAffineAttrsPush_payload_objAttrs_affine_mat_0_1,
  //  input           objAffineAttrsPush_payload_objAttrs_affine_mat_1_0,
  //  input           objAffineAttrsPush_payload_objAttrs_affine_mat_1_1,
  //  input           objAffineAttrsPush_payload_objAttrs_size2d_x,
  //  input           objAffineAttrsPush_payload_objAttrs_size2d_y,
  //  input           objAffineAttrsPush_payload_objAttrs_dispFlip_x,
  //  input           objAffineAttrsPush_payload_objAttrs_dispFlip_y,
  //  input           objAffineAttrsPush_payload_memIdx,
  //  input           objPalEntryPush_valid,
  //  input           objPalEntryPush_payload_objPalEntry_col_r,
  //  input           objPalEntryPush_payload_objPalEntry_col_g,
  //  input           objPalEntryPush_payload_objPalEntry_col_b,
  //  input           objPalEntryPush_payload_memIdx
  //);

  //modport mst (
  //  output          colorMathTilePush_valid,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_0,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_1,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_2,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_3,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_4,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_5,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_6,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_7,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_8,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_9,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_10,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_11,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_12,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_13,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_14,
  //  output          colorMathTilePush_payload_tileSlice_colIdxVec_15,
  //  output          colorMathTilePush_payload_memIdx,
  //  output          colorMathEntryPush_valid,
  //  output          colorMathEntryPush_payload_bgEntry_tileIdx,
  //  output          colorMathEntryPush_payload_bgEntry_dispFlip_x,
  //  output          colorMathEntryPush_payload_bgEntry_dispFlip_y,
  //  output          colorMathEntryPush_payload_memIdx,
  //  output          colorMathAttrsPush_valid,
  //  output          colorMathAttrsPush_payload_bgAttrs_scroll_x,
  //  output          colorMathAttrsPush_payload_bgAttrs_scroll_y,
  //  output          colorMathAttrsPush_payload_bgAttrs_fbAttrs_doIt,
  //  output          colorMathAttrsPush_payload_bgAttrs_fbAttrs_tileMemBaseAddr,
  //  output          colorMathPalEntryPush_valid,
  //  output          colorMathPalEntryPush_payload_bgPalEntry_col_r,
  //  output          colorMathPalEntryPush_payload_bgPalEntry_col_g,
  //  output          colorMathPalEntryPush_payload_bgPalEntry_col_b,
  //  output          colorMathPalEntryPush_payload_memIdx,
  //  output          bgTilePush_valid,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_0,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_1,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_2,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_3,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_4,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_5,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_6,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_7,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_8,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_9,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_10,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_11,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_12,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_13,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_14,
  //  output          bgTilePush_payload_tileSlice_colIdxVec_15,
  //  output          bgTilePush_payload_memIdx,
  //  output          bgEntryPushVec_0_valid,
  //  output          bgEntryPushVec_0_payload_bgEntry_tileIdx,
  //  output          bgEntryPushVec_0_payload_bgEntry_dispFlip_x,
  //  output          bgEntryPushVec_0_payload_bgEntry_dispFlip_y,
  //  output          bgEntryPushVec_0_payload_memIdx,
  //  output          bgEntryPushVec_1_valid,
  //  output          bgEntryPushVec_1_payload_bgEntry_tileIdx,
  //  output          bgEntryPushVec_1_payload_bgEntry_dispFlip_x,
  //  output          bgEntryPushVec_1_payload_bgEntry_dispFlip_y,
  //  output          bgEntryPushVec_1_payload_memIdx,
  //  output          bgAttrsPushVec_0_valid,
  //  output          bgAttrsPushVec_0_payload_bgAttrs_scroll_x,
  //  output          bgAttrsPushVec_0_payload_bgAttrs_scroll_y,
  //  output          bgAttrsPushVec_0_payload_bgAttrs_fbAttrs_doIt,
  //  output          bgAttrsPushVec_0_payload_bgAttrs_fbAttrs_tileMemBaseAddr,
  //  output          bgAttrsPushVec_1_valid,
  //  output          bgAttrsPushVec_1_payload_bgAttrs_scroll_x,
  //  output          bgAttrsPushVec_1_payload_bgAttrs_scroll_y,
  //  output          bgAttrsPushVec_1_payload_bgAttrs_fbAttrs_doIt,
  //  output          bgAttrsPushVec_1_payload_bgAttrs_fbAttrs_tileMemBaseAddr,
  //  output          bgPalEntryPush_valid,
  //  output          bgPalEntryPush_payload_bgPalEntry_col_r,
  //  output          bgPalEntryPush_payload_bgPalEntry_col_g,
  //  output          bgPalEntryPush_payload_bgPalEntry_col_b,
  //  output          bgPalEntryPush_payload_memIdx,
  //  output          objTilePush_valid,
  //  input           objTilePush_ready,
  //  output          objTilePush_payload_tileSlice_colIdxVec_0,
  //  output          objTilePush_payload_tileSlice_colIdxVec_1,
  //  output          objTilePush_payload_tileSlice_colIdxVec_2,
  //  output          objTilePush_payload_tileSlice_colIdxVec_3,
  //  output          objTilePush_payload_memIdx,
  //  output          objAffineTilePush_valid,
  //  output          objAffineTilePush_payload_tilePx,
  //  output          objAffineTilePush_payload_memIdx,
  //  output          objAttrsPush_valid,
  //  output          objAttrsPush_payload_objAttrs_tileIdx,
  //  output          objAttrsPush_payload_objAttrs_pos_x,
  //  output          objAttrsPush_payload_objAttrs_pos_y,
  //  output          objAttrsPush_payload_objAttrs_prio,
  //  output          objAttrsPush_payload_objAttrs_size2d_x,
  //  output          objAttrsPush_payload_objAttrs_size2d_y,
  //  output          objAttrsPush_payload_objAttrs_dispFlip_x,
  //  output          objAttrsPush_payload_objAttrs_dispFlip_y,
  //  output          objAttrsPush_payload_memIdx,
  //  output          objAffineAttrsPush_valid,
  //  output          objAffineAttrsPush_payload_objAttrs_tileIdx,
  //  output          objAffineAttrsPush_payload_objAttrs_pos_x,
  //  output          objAffineAttrsPush_payload_objAttrs_pos_y,
  //  output          objAffineAttrsPush_payload_objAttrs_prio,
  //  output          objAffineAttrsPush_payload_objAttrs_affine_doIt,
  //  output          objAffineAttrsPush_payload_objAttrs_affine_mat_0_0,
  //  output          objAffineAttrsPush_payload_objAttrs_affine_mat_0_1,
  //  output          objAffineAttrsPush_payload_objAttrs_affine_mat_1_0,
  //  output          objAffineAttrsPush_payload_objAttrs_affine_mat_1_1,
  //  output          objAffineAttrsPush_payload_objAttrs_size2d_x,
  //  output          objAffineAttrsPush_payload_objAttrs_size2d_y,
  //  output          objAffineAttrsPush_payload_objAttrs_dispFlip_x,
  //  output          objAffineAttrsPush_payload_objAttrs_dispFlip_y,
  //  output          objAffineAttrsPush_payload_memIdx,
  //  output          objPalEntryPush_valid,
  //  output          objPalEntryPush_payload_objPalEntry_col_r,
  //  output          objPalEntryPush_payload_objPalEntry_col_g,
  //  output          objPalEntryPush_payload_objPalEntry_col_b,
  //  output          objPalEntryPush_payload_memIdx
  //);

endinterface

module Gpu2d(
	Gpu2dIo/*.mst*/ io,
	input logic clk,
	input logic rst
);
endmodule

module TopLevel(
	input logic clk,
	input logic rst
);
	Gpu2dIo gpu2d_io ();
	Gpu2d gpu2d(
		.io(gpu2d_io),
		.clk(clk),
		.rst(rst)
	);
endmodule
