// Generator : SpinalHDL dev    git head : 96fd536acf731cde33101dc1a48387f910482693
// Component : InterfaceTest
// Git hash  : 8150cbc31bcd0385a7234f2b4c7189b29982a3b5

`timescale 1ns/1ps

interface InterfaceTestIo () ;

  TestIntf        inpWord();
  TestIntf        outpWord();

endinterface

interface TestIntf () ;

  logic  [7:0]    myWord ;

endinterface


module InterfaceTest (
  InterfaceTestIo      io,
  input  wire          clk,
  input  wire          reset
);


  always @(posedge clk) begin
    io.outpWord.myWord <= (io.inpWord.myWord + 8'h01);
  end


endmodule
