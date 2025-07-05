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
	parameter WIDTH = 33
)(
	input wire clk,
	//input wire rst,
	input wire signed [WIDTH - 1:0] a,
	input wire signed [WIDTH - 1:0] b,

	output reg signed [WIDTH - 1:0] outp 
);
	always @(posedge clk) begin
		//if (rst) begin
		//	outp <= 0;
		//end else begin
			outp <= a + b;
		//end
	end
endmodule
