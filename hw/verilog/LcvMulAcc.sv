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

(* use_dsp = "yes" *)
module LcvAluDel1 #(
	parameter int WIDTH=32
	//parameter int USE_DSP48=1
)(
	input logic clk,
	input logic signed [WIDTH - 1:0] inp_a,
	input logic signed [WIDTH - 1:0] inp_b_0,
	input logic signed [WIDTH - 1:0] inp_b_1,
	input logic inp_b_sel,
	input logic [/*2*//*0*/1:0] inp_op,
	output logic signed [WIDTH - 1:0] outp_data
);
	wire signed [WIDTH - 1:0] temp_inp_b = inp_b_sel ? inp_b_0 : inp_b_1;
	always_ff @(posedge clk) begin
		case (inp_op)
		//--------
		2'h0: begin
			outp_data <= inp_a + temp_inp_b;
		end
		2'h1: begin
			outp_data <= inp_a - temp_inp_b;
		end
		2'h2: begin
			outp_data <= inp_a & temp_inp_b;
		end
		2'h3: begin
			outp_data <= inp_a | temp_in_b;
		end
		//3'h2: begin
		//	outp_data <= inp_a & temp_inp_b;
		//end
		//3'h3: begin
		//	outp_data <= inp_a | temp_inp_b;
		//end
		//3'h4: begin
		//	outp_data <= inp_a ^ temp_inp_b;
		//end
		////3'h5: begin
		////	outp_data[0] <= $unsigned(inp_a) < $unsigned(temp_inp_b);
		////	outp_data[WIDTH - 1:1] <= 'h0;
		////end
		////3'h6: begin
		////	outp_data[0] <= $signed(inp_a) < $signed(temp_inp_b);
		////	outp_data[WIDTH - 1:1] <= 'h0;
		////end
		////3'h7: 
		//default: begin
		//	//outp_data <= 'h0;
		//	outp_data <= inp_a & temp_inp_b;
		//end
		//--------
		endcase
	end
endmodule

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
