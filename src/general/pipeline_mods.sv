`include "../include/misc_preprocs.svh"

interface PipeSkidBufSideIntf #(
	type DataT
);
	logic valid = 1'b0, ready = 1'b0;
	DataT data = '0;

	//modport Next(
	//	output valid, data,
	//	input ready
	//);
	//modport Prev(
	//	input valid, data,
	//	output ready
	//);
endinterface

interface PipeSkidBufMiscIntf #(
	bit OPT_INCLUDE_VALID_BUSY, OPT_INCLUDE_READY_BUSY
);
	generate
		if (OPT_INCLUDE_VALID_BUSY) begin: vb
			logic valid_busy = 1'b0;
		end
		if (OPT_INCLUDE_READY_BUSY) begin: rb
			logic ready_busy = 1'b0;
		end
	endgenerate
	logic clear;
endinterface

interface PipeSkidBufIntf #(
	type DataT,
	bit OPT_INCLUDE_VALID_BUSY, OPT_INCLUDE_READY_BUSY
);
	logic clk = 1'b0, rst = 1'b0;
	PipeSkidBufSideIntf #(.DataT(DataT)) next(), prev();
	PipeSkidBufMiscIntf #(
		.OPT_INCLUDE_VALID_BUSY(OPT_INCLUDE_VALID_BUSY),
		.OPT_INCLUDE_READY_BUSY(OPT_INCLUDE_READY_BUSY)
	) misc();
endinterface

module PipeSkidBuf
#(
	type DataT,
	bit OPT_INCLUDE_VALID_BUSY, OPT_INCLUDE_READY_BUSY,
	bit OPT_PASSTHROUGH, OPT_TIE_IFWD_VALID
) (
	PipeSkidBufIntf intf
);
	wire DataT
		ifwd_data = intf.prev.data,
		ofwd_data;
	wire
		ifwd_valid = intf.prev.valid,
		ofwd_valid,
		ibak_ready = intf.next.ready,
		obak_ready,
		full_rst = intf.rst || intf.misc.clear;
	assign {
		intf.next.data,
		intf.next.valid,
		intf.prev.ready
	} = {
		ofwd_data,
		ofwd_valid,
		obak_ready
	};
	logic
		temp_valid_busy = 1'b0,
		temp_ready_busy = 1'b0;
	generate
		//--------
		//generate
		//	if (OPT_INCLUDE_VALID_BUSY) begin: lab_valid_busy
		//		always_comb begin
		//			temp_valid_busy = intf.misc.vb.valid_busy;
		//		end
		//	end
		//	if (OPT_INCLUDE_READY_BUSY) begin: lab_ready_busy
		//		always_comb begin
		//			temp_ready_busy = intf.misc.rb.ready_busy;
		//		end
		//	end
		//endgenerate
		//--------
		if (OPT_PASSTHROUGH) begin: lab_pass
			assign {
				ofwd_valid,
				obak_ready
			} = {
				ifwd_valid,
				ibak_ready
			};
			always_ff @(posedge intf.clk) begin
				if (!intf.rst) begin
					intf.next.data <= intf.prev.data;
				end
			end
		end else begin: lab_not_pass // if (!OPT_PASSTHROUGH)
			//--------
			DataT
				r_data = '0, r_data_next = '0;
			logic
				r_valid = 1'b0, r_valid_next = 1'b0, r_ready = 1'b0;

			//always_ff @(posedge intf.clk)
			always_comb
			begin
				if (full_rst) begin
					r_valid_next = 1'b0;
				end else if (
					ifwd_valid && obak_ready
					&& ofwd_valid && !ibak_ready
				) begin
					r_valid_next = 1'b1;
				end else if (ibak_ready) begin
					r_valid_next = 1'b0;
				end else begin
					r_valid_next = r_valid;
				end

				if (full_rst) begin
					r_data_next = '0;
				end else if (obak_ready) begin
					r_data_next = intf.prev.data;
				end
			end
			always_ff @(posedge intf.clk) begin
				r_data <= r_data_next;

				if (!temp_valid_busy) begin
					r_valid <= r_valid_next;
				end else begin // if (temp_valid_busy)
					r_valid <= 1'b0;
				end

				if (!temp_ready_busy) begin
					r_ready <= !r_valid_next;
				end else begin // if (temp_ready_busy)
					r_ready <= 1'b0;
				end
			end

			always_comb begin
				if (full_rst) begin
					//{obak_ready, ofwd_valid} = '0;
					obak_ready = 1'b0;
					ofwd_valid = 1'b0;
				end else begin
					//obak_ready = !r_valid;
					obak_ready = r_ready;
					ofwd_valid = ifwd_valid || r_valid;
				end
			end

			always_comb begin
				if (r_valid) begin
					//intf.next.data = r_data;
					ofwd_data = r_data;
				end else begin // if (!r_valid)
					//intf.next.data = intf.prev.data;
					ofwd_data = ifwd_data;
				end
			end
			//--------
			`ifdef FORMAL
			logic
				past_valid = 1'b0;
				//test_not_valid_busy = 1'b1,
				//test_not_ready_busy = 1'b1;

			generate
				if (OPT_INCLUDE_VALID_BUSY) begin: lab_test_valid_busy
					//always_comb begin
					//	test_not_valid_busy = !intf.misc.vb.valid_busy;
					//end
					//assume(!temp_valid_busy);
					always @(posedge intf.clk) begin
						assume(!intf.misc.vb.valid_busy);
					end
				end
				if (OPT_INCLUDE_READY_BUSY) begin: lab_test_ready_busy
					//always_comb begin
					//	test_not_ready_busy = !intf.misc.rb.ready_busy;
					//end
					//assume(!temp_ready_busy);
					always @(posedge intf.clk) begin
						assume(!intf.misc.rb.ready_busy);
					end
				end
			endgenerate

			always_ff @(posedge intf.clk) begin
				//if (
				//	full_rst
				//) begin
					past_valid <= 1'b1;
				//end
			end

			always @(posedge intf.clk) begin
				if (!past_valid) begin
					assume(intf.rst);
				end else begin // if (past_valid)
					assume(!intf.rst);
				end
			end
			always @(posedge intf.clk) begin
				if (past_valid && !full_rst) begin
					if ($past(ofwd_valid) && !$past(ibak_ready)) begin
						//assert (ofwd_valid && $stable(ofwd_data));
						assert(ofwd_valid);
						//assert(ifwd_valid || r_valid);
						assert($stable(ofwd_data));
						//assert(ofwd_data == $past(ofwd_data));
					end
				end
			end
			//assert property (
			//	@(posedge intf.clk)
			//	disable iff (intf.rst)
			//	(
			//		ifwd_valid && obak_ready
			//		&& ofwd_valid && !ibak_ready
			//	) |=> (r_valid && r_data == (ifwd_data))
			//);
			always @(posedge intf.clk) begin
			//always_comb begin
				if (past_valid && !full_rst) begin
					if (
						ifwd_valid && obak_ready
						&& ofwd_valid && !ibak_ready
					) begin
						assert(r_valid_next);
						assert(r_data_next == ifwd_data);
					end
				end
			end

			//assert property (
			//	@(posedge intf.clk)
			//	disable iff (intf.rst)
			//	ibak_ready |=> (ofwd_valid == ifwd_valid)
			//);
			//assert property (
			//	@(posedge intf.clk)
			//	disable iff (intf.rst)
			//	ifwd_valid && obak_ready |=> ofwd_valid
			//);
			always @(posedge intf.clk) begin
			//always_comb begin
				if (past_valid && !full_rst) begin
					if (ifwd_valid && obak_ready) begin
						assert(ofwd_valid);
					end
				end
			end

			//assert property (
			//	@(posedge intf.clk)
			//	disable iff (intf.rst)
			//	!ifwd_valid && !r_valid && ibak_ready |=> !ofwd_valid
			//);
			always @(posedge intf.clk) begin
			//always_comb begin
				if (past_valid && !full_rst) begin
					if (
						!ifwd_valid
						&& !r_valid
						&& ibak_ready
					) begin
						assert(!ofwd_valid);
					end
				end
			end

			//assert property (
			//	@(posedge intf.clk)
			//	r_valid && ibak_ready |=> !r_valid
			//);
			always @(posedge intf.clk) begin
			//always_comb begin
				if (past_valid && !full_rst) begin
					if ($past(r_valid) && ibak_ready) begin
						assert(!r_valid_next);
					end
				end
			end
			`endif		// FORMAL
			//--------
		end
	endgenerate
endmodule

module Top #(
	type DataT=struct packed { bit [16] x, y; },
	bit OPT_INCLUDE_VALID_BUSY=1'b0, OPT_INCLUDE_READY_BUSY=1'b0,
	bit OPT_PASSTHROUGH=1'b0, OPT_TIE_IFWD_VALID=1'b0
) (
	logic clk, rst,

	input DataT prev_data,
	input logic prev_valid,
	output logic prev_ready, 

	output DataT next_data,
	output logic next_valid,
	input logic next_ready
);
	PipeSkidBufIntf #(
		.DataT(DataT),
		.OPT_INCLUDE_VALID_BUSY(OPT_INCLUDE_VALID_BUSY),
		.OPT_INCLUDE_READY_BUSY(OPT_INCLUDE_READY_BUSY)
	) psb_intf();
	PipeSkidBuf #(
		.DataT(DataT),
		.OPT_INCLUDE_VALID_BUSY(OPT_INCLUDE_VALID_BUSY),
		.OPT_INCLUDE_READY_BUSY(OPT_INCLUDE_READY_BUSY),
		.OPT_PASSTHROUGH(OPT_PASSTHROUGH),
		.OPT_TIE_IFWD_VALID(OPT_TIE_IFWD_VALID)
	) psb(.intf(psb_intf));

	assign {
		psb_intf.clk, psb_intf.rst,
		psb_intf.prev.data, psb_intf.prev.valid,
		psb_intf.next.ready
	} = {
		clk, rst,
		prev_data, prev_valid,
		next_ready
	};
	assign {
		prev_ready,
		next_data, next_valid
	} = {
		psb_intf.prev.ready,
		psb_intf.next.data, psb_intf.next.valid
	};
endmodule
