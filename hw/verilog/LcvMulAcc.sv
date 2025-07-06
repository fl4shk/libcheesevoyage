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
	parameter WIDTH=32
)(
	input logic clk,
	//input logic rst,
	input logic signed [WIDTH - 1:0] inp_a,
	input logic signed [WIDTH - 1:0] inp_b,
	input logic inp_carry,
	//input logic do_inv,

	output logic signed [WIDTH:0] outp_sum_carry
	//output logic outp_carry 
);
	//reg signed [WIDTH - 1:0] r_a;
	//reg signed [WIDTH - 1:0] r_b;
	//reg r_carry_in;
	wire signed [WIDTH:0] temp_a = {1'b0, inp_a};
	wire signed [WIDTH:0] temp_b = {1'b0, inp_b};
	wire signed [WIDTH:0] temp_carry = {{(WIDTH){1'b0}}, inp_carry};

	wire signed [WIDTH:0] temp_sum = (
		temp_a + temp_b + temp_carry
	);
	//assign outp_sum = (!do_inv) ? temp_sum : ~temp_sum;

	always @(posedge clk) begin
		//{outp_carry, outp_sum} <= temp_sum;
		outp_sum_carry <= temp_sum;
		//outp_sum <= (!do_inv) ? temp_sum : ~temp_sum;
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
	wire signed [WIDTH:0] my_a = $signed({1'b0, a});
	wire signed [WIDTH:0] my_b = $signed({1'b1, b});
	wire signed [WIDTH:0] my_carry_in = (
		$signed({{(WIDTH){1'b0}}, 1'b1})
	);
	always @(posedge clk) begin
		outp_data <= (
			$signed(
				$signed(($signed(my_a) ^ (~$signed(my_b))))
				+ my_carry_in
			)
		);
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
