`default_nettype none
module Top (
	clk,
	rst,
	prev_data,
	prev_valid,
	prev_ready,
	next_data,
	next_valid,
	next_ready
);
	parameter [0:0] OPT_INCLUDE_VALID_BUSY = 1'b1;
	parameter [0:0] OPT_INCLUDE_READY_BUSY = 1'b0;
	parameter [0:0] OPT_PASSTHROUGH = 1'b1;
	parameter [0:0] OPT_TIE_IFWD_VALID = 1'b0;
	input wire clk;
	input wire rst;
	input wire [1:0] prev_data;
	input wire prev_valid;
	output wire prev_ready;
	output wire [1:0] next_data;
	output wire next_valid;
	input wire next_ready;
	localparam _param_F1CED_OPT_INCLUDE_VALID_BUSY = OPT_INCLUDE_VALID_BUSY;
	localparam _param_F1CED_OPT_INCLUDE_READY_BUSY = OPT_INCLUDE_READY_BUSY;
	generate
		if (1) begin : psb_intf
			localparam [0:0] OPT_INCLUDE_VALID_BUSY = _param_F1CED_OPT_INCLUDE_VALID_BUSY;
			localparam [0:0] OPT_INCLUDE_READY_BUSY = _param_F1CED_OPT_INCLUDE_READY_BUSY;
			wire clk;
			wire rst;
			if (1) begin : next
				reg valid;
				wire ready;
				reg [1:0] data;
			end
			if (1) begin : prev
				wire valid;
				reg ready;
				wire [1:0] data;
			end
			localparam _param_3F3C6_OPT_INCLUDE_VALID_BUSY = OPT_INCLUDE_VALID_BUSY;
			localparam _param_3F3C6_OPT_INCLUDE_READY_BUSY = OPT_INCLUDE_READY_BUSY;
			if (1) begin : misc
				localparam [0:0] OPT_INCLUDE_VALID_BUSY = _param_3F3C6_OPT_INCLUDE_VALID_BUSY;
				localparam [0:0] OPT_INCLUDE_READY_BUSY = _param_3F3C6_OPT_INCLUDE_READY_BUSY;
				if (OPT_INCLUDE_VALID_BUSY) begin : lab_valid_busy
					wire valid_busy;
				end
				if (OPT_INCLUDE_READY_BUSY) begin : lab_ready_busy
					wire ready_busy;
				end
				wire clear;
			end
		end
	endgenerate
	localparam _param_10533_OPT_INCLUDE_VALID_BUSY = OPT_INCLUDE_VALID_BUSY;
	localparam _param_10533_OPT_INCLUDE_READY_BUSY = OPT_INCLUDE_READY_BUSY;
	localparam _param_10533_OPT_PASSTHROUGH = OPT_PASSTHROUGH;
	localparam _param_10533_OPT_TIE_IFWD_VALID = OPT_TIE_IFWD_VALID;
	generate
		if (1) begin : psb
			localparam [0:0] OPT_INCLUDE_VALID_BUSY = _param_10533_OPT_INCLUDE_VALID_BUSY;
			localparam [0:0] OPT_INCLUDE_READY_BUSY = _param_10533_OPT_INCLUDE_READY_BUSY;
			localparam [0:0] OPT_PASSTHROUGH = _param_10533_OPT_PASSTHROUGH;
			localparam [0:0] OPT_TIE_IFWD_VALID = _param_10533_OPT_TIE_IFWD_VALID;
			if (OPT_PASSTHROUGH) begin : lab_pass
				wire [2:1] sv2v_tmp_CF22A;
				assign sv2v_tmp_CF22A = {Top.psb_intf.prev.valid, Top.psb_intf.next.ready};
				always @(*) {Top.psb_intf.next.valid, Top.psb_intf.prev.ready} = sv2v_tmp_CF22A;
				always_ff @(posedge Top.psb_intf.clk)
					if (!Top.psb_intf.rst)
						Top.psb_intf.next.data <= Top.psb_intf.prev.data;
			end
			else begin : lab_not_pass
				reg [1:0] r_data;
				reg r_valid;
				wire r_ready;
				always_ff @(posedge Top.psb_intf.clk) begin
					if (Top.psb_intf.rst || Top.psb_intf.misc.clear)
						r_valid <= 1'b0;
					else if ((Top.psb_intf.prev.valid && Top.psb_intf.prev.ready) && (Top.psb_intf.next.valid && !Top.psb_intf.next.ready))
						r_valid <= 1'b1;
					else if (Top.psb_intf.next.ready)
						r_valid <= 1'b0;
					if (Top.psb_intf.rst || Top.psb_intf.misc.clear)
						r_data <= 1'sb0;
					else
						r_data <= Top.psb_intf.prev.data;
				end
				always_comb if (Top.psb_intf.rst || Top.psb_intf.misc.clear)
					{Top.psb_intf.prev.ready, Top.psb_intf.next.valid} = 1'sb0;
				else begin
					Top.psb_intf.prev.ready = ~r_valid;
					Top.psb_intf.next.valid = Top.psb_intf.prev.valid || r_valid;
				end
				always_comb if (r_valid)
					Top.psb_intf.next.data = r_data;
				else
					Top.psb_intf.next.data = Top.psb_intf.prev.data;
				reg past_valid = 1'b0;
				reg test_not_valid_busy = 1'b1;
				reg test_not_ready_busy = 1'b1;
				if (OPT_INCLUDE_VALID_BUSY) begin : lab_test_valid_busy
					always_comb test_not_valid_busy = ~Top.psb_intf.misc.valid_busy;
				end
				if (OPT_INCLUDE_READY_BUSY) begin : lab_test_ready_busy
					always_comb test_not_ready_busy = ~Top.psb_intf.misc.ready_busy;
				end
				always_ff @(posedge Top.psb_intf.clk) begin
					past_valid <= 1'b1;
					if (((past_valid && !Top.psb_intf.misc.clear) && test_not_valid_busy) && test_not_ready_busy) begin
						if (Top.psb_intf.rst) begin
							if (!OPT_TIE_IFWD_VALID)
								assert (!Top.psb_intf.prev.valid) ;
							assert (!r_valid && !Top.psb_intf.next.valid) ;
						end
						else begin
							if (Top.psb_intf.prev.valid && !Top.psb_intf.prev.ready) begin
								assert (Top.psb_intf.next.valid) ;
								assert ($stable(Top.psb_intf.prev.data)) ;
							end
							if (Top.psb_intf.next.valid && !Top.psb_intf.next.ready) begin
								assert (Top.psb_intf.prev.ready) ;
								assert ($stable(Top.psb_intf.next.data)) ;
							end
							if (((Top.psb_intf.prev.valid && Top.psb_intf.prev.ready) && Top.psb_intf.next.valid) && !Top.psb_intf.next.ready) begin
								assert (r_valid) ;
								assert (r_data == $past(Top.psb_intf.prev.data)) ;
							end
							if ((!Top.psb_intf.prev.valid && !r_valid) && Top.psb_intf.next.ready)
								assert (Top.psb_intf.next.valid) ;
							if (r_valid && Top.psb_intf.next.ready)
								assert (!r_valid) ;
						end
					end
				end
			end
		end
	endgenerate
	assign {psb_intf.clk, psb_intf.rst, psb_intf.prev.data, psb_intf.prev.valid, psb_intf.next.ready} = {clk, rst, prev_data, prev_valid, next_ready};
	assign {prev_ready, next_data, next_valid} = {psb_intf.prev.ready, psb_intf.next.data, psb_intf.next.valid};
endmodule
