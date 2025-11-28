// Alliance AS4C32M16SB - 512Mbit (4x8Mx16) SDRAM
//
// Copyright (c) 2025 David Hunter
//
// Portions copied from sdram.v:
//   sdram controller implementation
//   Copyright (c) 2018 Sorgelig
//
// This program is GPL licensed. See COPYING for the full license.
//
// FL4SHK NOTE: obtained from here:
// https://raw.githubusercontent.com/ReverendGumby/Test_V810_MiSTer/refs/heads/main/rtl/tb/as4c32m16sb.sv

// TODO:
// - Enforce tRCD, tRAS
// - Enforce bank precharge and activation

module as4c32m16sb
    (
	 inout [15:0] DQ, // 16 bit bidirectional data bus
	 input [12:0] A, // 13 bit multiplexed address bus
	 input        DQML, // byte mask
	 input        DQMH, // byte mask
	 input [1:0]  BA, // two banks
	 input        nCS, // a single chip select
	 input        nWE, // write enable
	 input        nRAS, // row address select
	 input        nCAS, // columns address select
	 input        CLK,
	 input        CKE
     );

localparam CMD_NOP             = 3'b111;
localparam CMD_BURST_TERMINATE = 3'b110;
localparam CMD_READ            = 3'b101;
localparam CMD_WRITE           = 3'b100;
localparam CMD_ACTIVE          = 3'b011;
localparam CMD_PRECHARGE       = 3'b010;
localparam CMD_AUTO_REFRESH    = 3'b001;
localparam CMD_LOAD_MODE       = 3'b000;

int 			bl;
int             cas_latency;
int             cas_cnt, rd_cnt, wr_cnt;

logic [3:0] 	cmd;
logic [1:0]     ba;
logic [12:0]    a;
logic           cke;
logic [15:0]    din, dout;
logic           rden;
logic [15:0]    wr_din;
logic           dqml, dqmh;
logic           dqloe, dqhoe;

logic [1:0] 	bank;
logic [12:0]    row;
logic [9:0]     col;

logic [15:0] 	mem[1<<2][1<<13][1<<10];

task write(input [1:0] bank, input [12:0] row, input [9:0] col, input [15:0] d);
    mem[bank][row][col] = d;
endtask

always @(posedge CLK) begin
    cmd <= {nCS, nRAS, nCAS, nWE};
    a <= A;
    dqml <= DQML;
    dqmh <= DQMH;
    ba <= BA;
    cke <= CKE;
    din <= DQ;
end

initial begin
    cas_cnt = 0;
    rd_cnt = 0;
    wr_cnt = 0;
end

always @(posedge CLK) if (cke & ~cmd[3]) begin
    case (cmd[2:0])
        CMD_LOAD_MODE: begin
            // A[2:0] = burst length: 0-3=2^N, 7=512 (full page)
            if (A[2:0] < 3'd7)
                bl = 1 << a[2:0];
            else
                bl = 512;
            // A[6:4] = CAS latency
            cas_latency = int'(a[6:4]);
            // TODO: Write Burst Length, Burst Type
        end
        CMD_ACTIVE: begin
            row <= a[12:0];
            bank <= ba;
        end
        CMD_READ: begin
            col <= a[9:0];
            bank <= ba;
            cas_cnt <= cas_latency - 2;
            rd_cnt <= bl;
        end
        CMD_WRITE: begin
            col <= a[9:0];
            bank <= ba;
            wr_din <= din;
            wr_cnt <= bl;
        end
        default: ;
    endcase
end

assign dout = mem[bank][row][col];

always @(posedge CLK) if (cke) begin
    if (cas_cnt != 0) begin
        cas_cnt <= cas_cnt - 1;
    end
    else if (rd_cnt != 0) begin
        rd_cnt <= rd_cnt - 1;
        col <= col + 1'd1;
    end
end

always @(posedge CLK) if (cke) begin
    if (wr_cnt != 0) begin
        if (~dqmh)
            mem[bank][row][col][15:8] <= wr_din[15:8];
        if (~dqml)
            mem[bank][row][col][7:0] <= wr_din[7:0];
        col <= col + 1'd1;
        wr_cnt <= wr_cnt - 1;
    end
end

assign rden = (cas_cnt == 0) & (rd_cnt != 0);
assign {dqhoe, dqloe} = {2{rden}} & {~dqmh, ~dqml};

assign DQ[15:8] = dqhoe ? dout[15:8] : 'Z;
assign DQ[7:0]  = dqloe ? dout[7:0]  : 'Z;

endmodule
