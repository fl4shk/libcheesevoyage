(* use_dsp48 = "yes" *)
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

(* use_dsp48 = "yes" *)
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
	input logic clk,
	//input logic rst,
	input logic signed [WIDTH - 1:0] a,
	input logic signed [WIDTH - 1:0] b,
	input logic carry_in,
	input logic do_inv,

	output logic signed [WIDTH - 1:0] outp_sum
);
	//reg signed [WIDTH - 1:0] r_a;
	//reg signed [WIDTH - 1:0] r_b;
	//reg r_carry_in;

	wire signed [WIDTH - 1:0] temp_sum;
	assign temp_sum = (
		a + b + $signed({{(WIDTH - 1){1'b0}}, carry_in})
	);
	//assign outp_sum = (!do_inv) ? temp_sum : ~temp_sum;

	always @(posedge clk) begin
		outp_sum <= (!do_inv) ? temp_sum : ~temp_sum;
		//r_a <= a;
		//r_b <= b;
		//r_carry_in <= carry_in;
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

(* use_dsp48 = "yes" *)
module LcvCmpEqDel1 #(
	parameter WIDTH=32
)(
	input logic clk,
	input logic signed [WIDTH - 1:0] a,
	input logic signed [WIDTH - 1:0] b,
	output logic signed [WIDTH:0] outp_data
);
	wire signed [WIDTH:0] my_a = {1'b0, a};
	wire signed [WIDTH:0] my_b = {1'b0, b};
	wire signed [WIDTH:0] my_carry_in = {{WIDTH{1'b0}}, 1'b1};
	always @(posedge clk) begin
		outp_data <= my_a ^ (~my_b) + my_carry_in;
		//outp_sum <= a + b;
	end
endmodule

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
