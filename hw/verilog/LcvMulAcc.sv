(* use_dsp = "yes" *)
module LcvMulAcc32(
	input logic signed [15:0] a,
	input logic signed [15:0] b,
	input logic signed [32:0] c,
	input logic signed [32:0] d,
	input logic signed [32:0] e,
	output logic signed [32:0] outp
);
	logic signed [35:0] pcout;
	assign pcout = a * b + c;
	assign outp = pcout + d + e;
endmodule

(* use_dsp = "yes" *)
module LcvMulAcc32Del1(
	input logic clk,
	input logic rst,
	input logic signed [15:0] a,
	input logic signed [15:0] b,
	input logic signed [32:0] c,
	input logic signed [32:0] d,
	input logic signed [32:0] e,
	output logic signed [32:0] outp
);
	//--------
	wire signed [35:0] pcout;
	always_ff @(posedge clk) begin
		//if (rst) begin
		//end else begin
		//end
		outp <= pcout + d + e;
	end
	assign pcout = a * b + c;
	//assign outp = pcout + d + e;
	//--------
	//always_ff @(posedge clk) begin
	//end
	//--------
endmodule

(* use_dsp = "yes" *)
module LcvAdcDel1 #(
	parameter WIDTH=32
)(
	input logic clk,
	//input logic rst,
	input logic signed [WIDTH - 1:0] inp_a,
	input logic signed [WIDTH - 1:0] inp_b,
	input logic inp_carry,

	output logic signed [WIDTH:0] outp_sum_carry
);
	wire signed [WIDTH:0] temp_a = {1'b0, inp_a};
	wire signed [WIDTH:0] temp_b = {1'b0, inp_b};
	wire signed [WIDTH:0] temp_carry = {{(WIDTH){1'b0}}, inp_carry};

	wire signed [WIDTH:0] temp_sum = (
		temp_a + temp_b + temp_carry
	);

	always_ff @(posedge clk) begin
		outp_sum_carry <= temp_sum;
	end
endmodule

(* use_dsp = "yes" *)
module LcvAddJustCarryDel1 #(
	parameter WIDTH=32
)(
	input logic clk,
	//input logic rst,
	input logic signed [WIDTH - 1:0] inp_a,
	//input logic signed [WIDTH - 1:0] inp_b,
	input logic inp_carry,

	output logic signed [WIDTH:0] outp_sum_carry
);
	wire signed [WIDTH:0] temp_a = {1'b0, inp_a};
	//wire signed [WIDTH:0] temp_b = {1'b0, inp_b};
	wire signed [WIDTH:0] temp_carry = {{(WIDTH){1'b0}}, inp_carry};

	wire signed [WIDTH:0] temp_sum = (
		//temp_a + temp_b + temp_carry
		temp_a + temp_carry
	);

	always_ff @(posedge clk) begin
		outp_sum_carry <= temp_sum;
	end
endmodule

(* use_dsp = "yes" *)
module LcvCondAddJustCarryDel1 #(
	parameter WIDTH=32
)(
	input logic clk,
	input logic signed [WIDTH - 1:0] inp_a,
	input logic inp_carry,
	input logic inp_cond,
	output logic signed [WIDTH:0] outp_sum_carry
);
	wire signed [WIDTH:0] temp_a = {1'b0, inp_a};
	wire signed [WIDTH:0] temp_carry = {{(WIDTH){1'b0}}, inp_carry};

	wire signed [WIDTH:0] temp_sum = (
		temp_a + temp_carry
	);

	always_ff @(posedge clk) begin
		if (inp_cond) begin
			outp_sum_carry <= temp_sum;
		end
	end
endmodule

(* use_dsp = "yes" *)
module LcvAddDel1 #(
	parameter WIDTH=32
)(
	input logic clk,
	//input logic rst,
	input logic signed [WIDTH - 1:0] inp_a,
	input logic signed [WIDTH - 1:0] inp_b,
	//input logic inp_carry,

	output logic signed [WIDTH:0] outp_sum_carry
);
	wire signed [WIDTH:0] temp_a = {1'b0, inp_a};
	wire signed [WIDTH:0] temp_b = {1'b0, inp_b};
	//wire signed [WIDTH:0] temp_carry = {{(WIDTH){1'b0}}, inp_carry};

	wire signed [WIDTH:0] temp_sum = (
		temp_a + temp_b //+ temp_carry
	);

	always_ff @(posedge clk) begin
		outp_sum_carry <= temp_sum;
	end
endmodule

(* use_dsp = "yes" *)
module LcvSubDel1 #(
	parameter WIDTH=32
)(
	input logic clk,
	input logic signed [WIDTH - 1:0] inp_a,
	input logic signed [WIDTH - 1:0] inp_b,
	//input logic inp_carry,

	output logic signed [WIDTH:0] outp_sum_carry
);
	wire signed [WIDTH:0] temp_a = {1'b0, inp_a};
	wire signed [WIDTH:0] temp_b = {1'b0, inp_b};

	wire signed [WIDTH:0] temp_sum = (
		temp_a - temp_b //+ temp_carry
	);

	always_ff @(posedge clk) begin
		outp_sum_carry <= temp_sum;
	end
endmodule

(* use_dsp = "yes" *)
module LcvCondSubDel1 #(
	parameter WIDTH=32
)(
	input logic clk,
	input logic signed [WIDTH - 1:0] inp_a,
	input logic signed [WIDTH - 1:0] inp_b,
	input logic inp_cond,
	output logic signed [WIDTH:0] outp_sum_carry
);
	wire signed [WIDTH:0] temp_a = {1'b0, inp_a};
	wire signed [WIDTH:0] temp_b = {1'b0, inp_b};
	//wire signed [WIDTH:0] temp_carry = {{(WIDTH){1'b0}}, inp_carry};

	wire signed [WIDTH:0] temp_sum = (
		//temp_a + temp_carry
		temp_a - temp_b
	);

	always_ff @(posedge clk) begin
		if (inp_cond) begin
			outp_sum_carry <= temp_sum;
		end
	end
endmodule


(* use_dsp = "yes" *)
module LcvCmpEqDel1 #(
	parameter WIDTH=32
)(
	input logic clk,
	input logic signed [WIDTH - 1:0] a,
	input logic signed [WIDTH - 1:0] b,
	output logic signed [WIDTH:0] outp_data
);
	wire signed [WIDTH:0] my_a = $signed({1'b0, a});
	wire signed [WIDTH:0] my_b = $signed({1'b1, b});
	wire signed [WIDTH:0] my_carry_in = (
		$signed({{(WIDTH){1'b0}}, 1'b1})
	);
	always_ff @(posedge clk) begin
		outp_data <= $signed({
			$signed(
				$signed(($signed(my_a) ^ (~$signed(my_b))))
				+ my_carry_in
			)
			//a === b,
			//{WIDTH{1'b0}}
		});
		//outp_sum <= a + b;
	end
endmodule

//(* use_dsp = "yes" *)
//module LcvCmpEqDel1 #(
//	parameter WIDTH=32
//)(
//	input wire clk,
//	input wire signed [WIDTH - 1:0] a,
//	input wire signed [WIDTH - 1:0] b,
//	input wire carry_in,
//
//	output reg signed [WIDTH - 1:0] outp_sum
//);
//	always_ff @(posedge clk) begin
//	end
//endmodule


// now let's try not having `use_dsp = "yes"`, as it seems you really need
// two pipeline stages for the DSP48 blocks
module LcvAluDel1 #(
	parameter int WIDTH=32
)(
	input logic clk,
	input logic signed [WIDTH - 1:0] inp_a,
	input logic signed [WIDTH - 1:0] inp_b_0,
	input logic signed [WIDTH - 1:0] inp_b_1,
	input logic [0:0] inp_b_sel,
	input logic [7:0] inp_op,
	output logic signed [WIDTH - 1:0] outp_data 
);
	//--------
	//localparam int SEL_SIZE = 2;
	localparam int OP_WIDTH = 8/*11*/;
	localparam [OP_WIDTH - 1:0] OP_ADD = 1 << 0;
	localparam [OP_WIDTH - 1:0] OP_SUB = 1 << 1;
	localparam [OP_WIDTH - 1:0] OP_SLTU /*OP_GET_INP_A*/ = 1 << 2;
	localparam [OP_WIDTH - 1:0] OP_SLTS /*OP_GET_INP_B*/ = 1 << 3;
	localparam [OP_WIDTH - 1:0] OP_AND = 1 << 4;
	localparam [OP_WIDTH - 1:0] OP_OR = 1 << 5;
	localparam [OP_WIDTH - 1:0] OP_XOR = 1 << 6;
	//localparam [OP_WIDTH - 1:0] OP_LSL = 1 << 7;
	//localparam [OP_WIDTH - 1:0] OP_LSR = 1 << 8;
	//localparam [OP_WIDTH - 1:0] OP_ASR = 1 << 9;
	localparam [OP_WIDTH - 1:0] /*OP_NOR*/ /*OP_ZERO*/ OP_ZERO = 1 << 7;
	//--------
	wire signed [WIDTH - 1:0] temp_inp_b = (
		inp_b_sel ? inp_b_1 : inp_b_0
	);
	//wire unsigned [WIDTH:0] temp_sum_u_inp_a = (
	//	$unsigned({1'b0, inp_a})
	//);
	//wire unsigned [WIDTH:0] temp_sum_u_inp_b = (
	//	$unsigned({1'b0, ~temp_inp_b})
	//);
	//wire unsigned [WIDTH:0] temp_sum_s_inp_a = (
	//	$unsigned({1'b0, ~inp_a[WIDTH - 1], inp_a[WIDTH - 2:0]})
	//);
	//wire unsigned [WIDTH:0] temp_sum_s_inp_b = (
	//	$unsigned({1'b0, temp_inp_b[WIDTH - 1], ~temp_inp_b[WIDTH - 2:0]})
	//);
	//wire unsigned [WIDTH:0] temp_sum_inp_carry = (
	//	$unsigned({{WIDTH{1'b0}}, 1'b1})
	//);
	//wire unsigned [WIDTH:0] temp_sum_u = (
	//	temp_sum_u_inp_a + temp_sum_u_inp_b + temp_sum_inp_carry
	//);
	//wire unsigned [WIDTH:0] temp_sum_s = (
	//	temp_sum_s_inp_a + temp_sum_s_inp_b + temp_sum_inp_carry
	//);
	//--------
	always_ff @(posedge clk) begin
		case (inp_op)
		OP_ADD: begin
			outp_data <= inp_a + temp_inp_b;
		end
		OP_SUB: begin
			outp_data <= inp_a - temp_inp_b;
		end
		OP_SLTU: begin
			outp_data[0] <= $unsigned(inp_a) < $unsigned(temp_inp_b);
			outp_data[WIDTH - 1:1] <= 'h0;
			//outp_data[0] <= ~temp_sum_u[WIDTH];
			//outp_data[WIDTH - 1:1] <= 'h0;
		end
		OP_SLTS: begin
			outp_data[0] <= $signed(inp_a) < $signed(temp_inp_b);
			outp_data[WIDTH - 1:1] <= 'h0;
			//outp_data[0] <= ~temp_sum_s[WIDTH];
			//outp_data[WIDTH - 1:1] <= 'h0;
		end
		OP_AND: begin
			outp_data <= inp_a & temp_inp_b;
		end
		OP_OR: begin
			outp_data <= inp_a | temp_inp_b;
		end
		OP_XOR: begin
			outp_data <= inp_a ^ temp_inp_b;
		end
		//OP_LSL: begin
		//	outp_data <= $unsigned(
		//		$unsigned(inp_a) << $unsigned(temp_inp_b)
		//	);
		//end
		//OP_LSR: begin
		//	outp_data <= $unsigned(
		//		$unsigned(inp_a) >> $unsigned(temp_inp_b)
		//	);
		//end
		//OP_ASR: begin
		//	outp_data <= $signed(
		//		$signed(inp_a) >>> $unsigned(temp_inp_b)
		//	);
		//end
		//OP_NOR: begin
		//	outp_data <= ~(inp_a | temp_inp_b);
		//end
		//OP_ZERO: 
		default: begin
			outp_data <= 'h0;
		end
		//OP_GET_INP_A: begin
		//	outp_data <= inp_a;
		//end
		//OP_GET_INP_B: begin
		//	outp_data <= temp_inp_b;
		//end
		endcase
	end
	//--------
endmodule

//(* use_dsp = "yes" *)
//module LcvAluDel1 #(
//	parameter int WIDTH=32
//	//parameter int USE_DSP48=1
//)(
//	input logic clk,
//	input logic signed [WIDTH - 1:0] inp_a,
//	input logic signed [WIDTH - 1:0] inp_b_0,
//	input logic signed [WIDTH - 1:0] inp_b_1,
//	input logic inp_b_sel,
//	input logic [/*2*/0/*1*/:0] inp_op,
//	output logic signed [WIDTH - 1:0] outp_data
//);
//	//wire signed [WIDTH - 1:0] temp_inp_b = inp_b_sel ? inp_b_0 : inp_b_1;
//	localparam int SEL_SIZE = 2;
//	//localparam int SEL_WIDTH = $clog2(SEL_SIZE);
//	logic signed [WIDTH - 1:0] r_outp_data_vec[SEL_SIZE];
//	logic r_inp_b_sel;
//
//	wire signed [WIDTH - 1:0] temp_inp_b_vec[SEL_SIZE];
//
//	generate
//		for (genvar i=0; i<SEL_SIZE; i+=1) begin
//			if (i == 0) begin
//				assign temp_inp_b_vec[i] = inp_b_0;
//			end else begin
//				assign temp_inp_b_vec[i] = inp_b_1;
//			end
//		end
//	endgenerate
//
//	initial begin
//		for (int i=0; i<SEL_SIZE; i+=1) begin
//			r_outp_data_vec[i] = 'h0;
//		end
//		r_inp_b_sel = 'h0;
//		outp_data = 'h0;
//	end
//
//	always_ff @(posedge clk) begin
//		r_inp_b_sel <= inp_b_sel;
//		for (int i=0; i<SEL_SIZE; i+=1) begin
//			case (inp_op)
//			//--------
//			1'h0: begin
//				r_outp_data_vec[i] <= inp_a + temp_inp_b_vec[i];
//			end
//			1'h1: begin
//				r_outp_data_vec[i] <= inp_a - temp_inp_b_vec[i];
//			end
//			endcase
//		end
//	end
//	assign outp_data = (
//		r_inp_b_sel ? r_outp_data_vec[1] : r_outp_data_vec[0]
//	);
//endmodule

//(* use_dsp = "yes" *)
//module LcvAluDel1 #(
//	parameter int WIDTH=32
//	//parameter int USE_DSP48=1
//)(
//	input logic clk,
//	input logic signed [WIDTH - 1:0] inp_a,
//	input logic signed [WIDTH - 1:0] inp_b_0,
//	input logic signed [WIDTH - 1:0] inp_b_1,
//	input logic inp_b_sel,
//	input logic [/*2*/0/*1*/:0] inp_op,
//	output logic signed [WIDTH - 1:0] outp_data
//);
//	//wire signed [WIDTH - 1:0] temp_inp_b = inp_b_sel ? inp_b_0 : inp_b_1;
//	localparam int SEL_SIZE = 2;
//	//localparam int SEL_WIDTH = $clog2(SEL_SIZE);
//	logic signed [WIDTH - 1:0] r_outp_data_vec[SEL_SIZE];
//	logic r_inp_b_sel;
//
//	wire signed [WIDTH - 1:0] temp_inp_b_vec[SEL_SIZE];
//
//	generate
//		for (genvar i=0; i<SEL_SIZE; i+=1) begin
//			if (i == 0) begin
//				assign temp_inp_b_vec[i] = inp_b_0;
//			end else begin
//				assign temp_inp_b_vec[i] = inp_b_1;
//			end
//		end
//	endgenerate
//
//	initial begin
//		for (int i=0; i<SEL_SIZE; i+=1) begin
//			r_outp_data_vec[i] = 'h0;
//		end
//		r_inp_b_sel = 'h0;
//		outp_data = 'h0;
//	end
//
//	always_ff @(posedge clk) begin
//		r_inp_b_sel <= inp_b_sel;
//		for (int i=0; i<SEL_SIZE; i+=1) begin
//			case (inp_op)
//			//--------
//			1'h0: begin
//				r_outp_data_vec[i] <= inp_a + temp_inp_b_vec[i];
//			end
//			1'h1: begin
//				r_outp_data_vec[i] <= inp_a - temp_inp_b_vec[i];
//			end
//			endcase
//		end
//	end
//	assign outp_data = (
//		r_inp_b_sel ? r_outp_data_vec[1] : r_outp_data_vec[0]
//	);
//endmodule

//(* use_dsp = "yes" *)
//module LcvAluDel1 #(
//	parameter WIDTH=32
//)(
//	input logic clk,
//	input logic signed [WIDTH - 1:0] inp_a,
//	input logic signed [WIDTH - 1:0] inp_b,
//	input logic signed [WIDTH - 1:0] inp_c,
//	input logic [1:0] inp_op,
//	output logic signed [WIDTH - 1:0] outp_data
//);
//	always_ff @(posedge clk) begin
//		case (inp_op)
//		//--------
//		2'h0: begin
//			outp_data <= inp_a + (inp_b + inp_c);
//		end
//		2'h1: begin
//			outp_data <= inp_a - (inp_b + inp_c);
//		end
//		2'h2: begin
//			outp_data <= inp_a & (inp_b + inp_c);
//		end
//		2'h3: begin
//			outp_data <= inp_a | (inp_b + inp_c);
//		end
//		//3'h2: begin
//		//	outp_data <= inp_a & temp_inp_b;
//		//end
//		//3'h3: begin
//		//	outp_data <= inp_a | temp_inp_b;
//		//end
//		//3'h4: begin
//		//	outp_data <= inp_a ^ temp_inp_b;
//		//end
//		////3'h5: begin
//		////	outp_data[0] <= $unsigned(inp_a) < $unsigned(temp_inp_b);
//		////	outp_data[WIDTH - 1:1] <= 'h0;
//		////end
//		////3'h6: begin
//		////	outp_data[0] <= $signed(inp_a) < $signed(temp_inp_b);
//		////	outp_data[WIDTH - 1:1] <= 'h0;
//		////end
//		////3'h7: 
//		//default: begin
//		//	//outp_data <= 'h0;
//		//	outp_data <= inp_a & temp_inp_b;
//		//end
//		//--------
//		endcase
//	end
//endmodule
