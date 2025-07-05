(* use_dsp48 = "yes" *)
module LcvMulAcc32(
	input wire signed [15:0] a,
	input wire signed [15:0] b,
	input wire signed [32:0] c,
	input wire signed [32:0] d,
	input wire signed [32:0] e,
	output wire signed [32:0] outp
);
	wire signed [35:0] pcout;
	assign pcout = a * b + c;
	assign outp = pcout + d + e;
endmodule

(* use_dsp48 = "yes" *)
module LcvMulAcc32Del1(
	input wire clk,
	input wire rst,
	input wire signed [15:0] a,
	input wire signed [15:0] b,
	input wire signed [32:0] c,
	input wire signed [32:0] d,
	input wire signed [32:0] e,
	output reg signed [32:0] outp
);
	//--------
	wire signed [35:0] pcout;
	always @(posedge clk) begin
		//if (rst) begin
		//end else begin
		//end
		outp <= pcout + d + e;
	end
	assign pcout = a * b + c;
	//assign outp = pcout + d + e;
	//--------
	//always @(posedge clk) begin
	//end
	//--------
endmodule

(* use_dsp48 = "yes" *)
module LcvAddDel1 #(
	parameter WIDTH=33
)(
	input wire clk,
	//input wire rst,
	input wire signed [WIDTH - 1:0] a,
	input wire signed [WIDTH - 1:0] b,
	input wire carry_in,
	input wire do_inv,

	output /*reg*/ wire signed [WIDTH - 1:0] outp_sum
);
	reg signed [WIDTH - 1:0] r_a;
	reg signed [WIDTH - 1:0] r_b;
	reg r_carry_in;

	wire signed [WIDTH - 1:0] temp_sum;
	assign temp_sum = r_a + r_b + $signed({{(WIDTH - 1){1'b0}}, r_carry_in});
	assign outp_sum = (!do_inv) ? temp_sum : ~temp_sum;

	always @(posedge clk) begin
		r_a <= a;
		r_b <= b;
		r_carry_in <= carry_in;
		//if (rst) begin
		//	outp <= 0;
		//end else begin
		//if (!do_inv) begin
		//	outp_sum <= temp_sum;
		//end else begin
		//	outp_sum <= ~temp_sum;
		//end
		//end
	end
endmodule

//(* use_dsp48 = "yes" *)
//module LcvSvCmpEqDel1 #(
//	parameter WIDTH=32
//)(
//	input wire clk,
//	input wire signed [WIDTH - 1:0] a,
//	input wire signed [WIDTH - 1:0] b,
//	input wire do_cmp_ne,
//	output wire signed [WIDTH - 1:0] outp_sum
//);
//	always @(posedge clk) begin
//		outp_sum <= a + b;
//	end
//endmodule

//(* use_dsp48 = "yes" *)
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
//	always @(posedge clk) begin
//	end
//endmodule
