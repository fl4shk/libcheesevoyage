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
	//input wire rst,
	input wire signed [15:0] a,
	input wire signed [15:0] b,
	input wire signed [32:0] c,
	input wire signed [32:0] d,
	input wire signed [32:0] e,
	output reg signed [32:0] outp
);
	//--------
	wire signed [35:0] pcout;
	assign pcout = a * b + c;
	//--------
	always @(posedge clk) begin
		outp <= pcout + d + e;
	end
	//--------
endmodule
