`include "../include/misc_preprocs.svh"
class long_div_cpkg;
	typedef struct {
		int unsigned MAIN_WIDTH, DENOM_WIDTH, CHUNK_WIDTH, TAG_WIDTH;
		bit USE_PIPE_SKID_BUF;
	} ConstantsT;
	localparam ConstantsT DEF_CONSTANTS = ConstantsT'{
		8, // MAIN_WIDTH
		8, // DENOM_WIDTH
		2, // CHUNK_WIDTH
		3, // TAG_WIDTH
		1'b1 // USE_PIPE_SKID_BUF
	};
endclass

//interface LongUdivIterData #(
//	parameter int unsigned MAIN_WIDTH, DENOM_WIDTH
//);
//endinterface

interface LongDivMulticycleToDivIntf #(
	parameter int unsigned MAIN_WIDTH=8, DENOM_WIDTH=8
);
	logic valid;

	logic [MAIN_WIDTH - 1:0] numer;
	logic [DENOM_WIDTH - 1:0] denom;
	logic is_signed;

	modport From(
		input valid, numer, denom, is_signed
	);
	modport To(
		output valid, numer, denom, is_signed
	);
endinterface
interface LongDivMulticycleFromDivIntf #(
	parameter int unsigned MAIN_WIDTH=8
);
	logic ready;

	logic [MAIN_WIDTH - 1:0] quot, rema;

	modport From(
		output ready, quot, rema
	);
	modport To(
		input ready, quot, rema
	);
endinterface

interface LongDivMulticycleIntf #(
	//parameter long_div_cpkg::ConstantsT constants=(
	//	long_div_cpkg::DEF_CONSTANTS
	//)
	parameter int unsigned MAIN_WIDTH=8, DENOM_WIDTH=8,
	//CHUNK_WIDTH=2,
	//parameter int unsigned TAG_WIDTH=3,
	//parameter bit USE_PIPE_SKID_BUF=1'b0
);
	//--------
	logic clk, rst;
	//--------
	LongDivMulticycleToDivIntf #(
		.MAIN_WIDTH(MAIN_WIDTH), .DENOM_WIDTH(DENOM_WIDTH)
	) to();
	LongDivMulticycleFromDivIntf #(
		.MAIN_WIDTH(MAIN_WIDTH)
	) from();
	//--------
endinterface

module LongDivMulticycle #(
	parameter int unsigned MAIN_WIDTH=8, DENOM_WIDTH=8, CHUNK_WIDTH=2
) (
	LongDivMulticycleIntf intf
);
	always_ff @(posedge intf.clk) begin
		if (intf.rst) begin
			//intf.from <= '0;
			{intf.from.ready, intf.from.quot, intf.from.rema} <= '0;
		end else begin // if (!from.rst)
			if (intf.to.valid) begin
				intf.from.ready <= 1'b1;
				//intf.from.quot <= intf.to.numer / intf.to.denom;
				//intf.from.rema <= intf.to.numer % intf.to.denom;
			end else begin // if (!intf.to.valid)
				intf.from.ready <= 1'b0;
			end
		end
	end
endmodule

//interface TestIntf #(
//	parameter int unsigned DATA_WIDTH, NUM_DIVS,
//	parameter int unsigned PARAM_TEST_ARR[NUM_DIVS]
//);
//	logic [DATA_WIDTH - 1:0] inp_data;
//	logic [$clog2(DATA_WIDTH) - 1:0] inp_sel;
//	logic [DATA_WIDTH - 1:0] outp_data;
//	logic [DATA_WIDTH - 1:0] test_arr
//
//	generate
//		for (genvar i=0; i<NUM_DIVS;
//endinterface
//
//module Top #(
//	//parameter long_div_cpkg::ConstantsT constants=(
//	//	long_div_cpkg::DEF_CONSTANTS
//	//)
//	parameter int unsigned MAIN_WIDTH=8, DENOM_WIDTH=8, CHUNK_WIDTH=2,
//	parameter int unsigned NUM_DIVS=3
//	//parameter int unsigned TAG_WIDTH=3,
//	//parameter bit USE_PIPE_SKID_BUF=1'b0
//) (
//	input logic clk, rst,
//	input logic inp_valid[NUM_DIVS],
//	input logic [MAIN_WIDTH - 1:0] inp_numer[NUM_DIVS],
//	input logic [DENOM_WIDTH - 1:0] inp_denom[NUM_DIVS],
//	input logic inp_is_signed[NUM_DIVS],
//	output logic outp_ready[NUM_DIVS],
//	output logic [MAIN_WIDTH - 1:0] outp_quot[NUM_DIVS],
//	output logic [MAIN_WIDTH - 1:0] outp_rema[NUM_DIVS]
//);
//	typedef int unsigned TestArrT[NUM_DIVS];
//	function TestArrT mk_test_arr();
//		int unsigned ret[NUM_DIVS];
//		for (int unsigned i=0; i<NUM_DIVS; i+=1) begin
//			ret[i] = i;
//		end
//		return ret;
//	endfunction
//
//	TestIntf #(
//		.DATA_WIDTH(MAIN_WIDTH),
//		.PARAM_TEST_ARR(mk_test_arr()),
//	) test_intf_arr[NUM_DIVS]();
//	//generate
//	//	for (genvar i=0; i<NUM_DIVS; i+=1) begin: gen_for_loop
//	//		//LongDivMulticycleIntf #(.constants(constants)) div_intf();
//	//		LongDivMulticycleIntf #(
//	//			.MAIN_WIDTH(MAIN_WIDTH), .DENOM_WIDTH(DENOM_WIDTH),
//	//		) div_intf();
//	//		LongDivMulticycle #(
//	//			.MAIN_WIDTH(MAIN_WIDTH), .DENOM_WIDTH(DENOM_WIDTH),
//	//			.CHUNK_WIDTH(CHUNK_WIDTH)
//	//		) div(
//	//			//.mp(div_intf.FromDiv)
//	//			.intf(div_intf)
//	//		);
//	//		assign div_intf.clk = clk;
//	//		assign div_intf.rst = rst;
//	//		assign div_intf.valid = inp_valid[i];
//	//		assign div_intf.numer = inp_numer[i];
//	//		assign div_intf.denom = inp_denom[i];
//	//		assign div_intf.is_signed = inp_is_signed[i];
//	//		assign outp_ready[i] = div_intf.ready;
//	//		assign outp_quot[i] = div_intf.quot;
//	//		assign outp_rema[i] = div_intf.rema;
//	//	end
//	//endgenerate
//endmodule
