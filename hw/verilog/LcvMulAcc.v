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
	output wire signed [32:0] outp
);
	//--------
	reg signed [35:0] pcout = 0;
	always @(posedge clk) begin
		//if (rst) begin
		//	pcout <= 36'd0;
		//end else begin
			pcout <= a * b + c;
		//end
	end
	//assign pcout = 
	assign outp = pcout + d + e;
	//--------
	//always @(posedge clk) begin
	//end
	//--------
endmodule
