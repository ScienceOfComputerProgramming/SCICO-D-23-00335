## PX-verification and other analyses

Recall that running test cases using an emotive agent produce _execution traces_, which by default (unless you use a custom tracing function) contain the emotion state of the agent at every update cycle. The traces are saved as csv-files. Emotions are put in columns named for example `Joy_goalname1`, `Joy_goalname2`, etc. So the name convention is _emotion-name_ followed by _goal-name_, separated by an '_'.

We can formulate specifications and check whether they are valid or satisfied by the tests by evaluating them on the the produced traces.

You can formulate a 'PX-specification' in the form of an _emotion pattern_. Here are some examples:

   * `"H;F"`: is satisfied by a trace where eventually there is an increase in hope and some time later an increase in fear.
   * `"H;NH;J"`: is satisfied by a trace where eventually there is an increase in hope, afterwhich it never increases again until there is an increase in joy.
   * `"NH->NJ"`: this is an implicative pattern. It is satisfied by a trace where either NH does not hold, or NJ holds. So, if hope never increases in a trace 𝜎 (NH), the implication holds on 𝜎 if joy never increases either (NJ).

The syntax of patterns is as follows:

   * _pattern_ ::= _seq-pattern_ ( '->'  _seq-pattern_)?
   * _seq-pattern_ ::= _emotion_ ( ';' _emotion_)*
   * _emotion_ ::= _increase_  | _absence_
   * _increase_ ::= H | J | S | F | D | P, representing future increase in, respectively, hope, joy, satisfaction, fear, distress, and disappointment emotion.
   * _absence_ ::=  NH | NJ | NS | NF | ND | NP, representing sustained absence of increase in, respectively, hope, joy, satisfaction, fear, distress, and disappointment emotion.

To verify patterns on a single trace, or a set of traces, use the class [`EmotionPattern`](../src/main/java/eu/iv4xr/ux/pxtesting/occ/EmotionPattern.java). The main methods are:

  * `checkOne(pattern, separator, fname)`: this loads a trace from the specified filename, and check whether or not the given _pattern_ holds on that trace. It returns a verdict which is either VALID, SAT, or UNKNOWN.
  * ` checkAll(pattern, separator, tracesDir, prefix) `: this loads traces from all trace-files in the given directory, and whose name starts with the prefix. The pattern is then checked on these traces. It returns an object the the type  `CheckingResult`, you can use its `toString()` to print the resulting judgement along with some statistics. The judgement is either:
     * VALID: no trace violates the pattern, and there is at least one trace that satisfies the pattern.
     * UNSAT: all traces either violate the pattern or result in UNKOWN, and moreover there is at least one trace that violates.
     * SAT: if there is one trace that satisfies the pattern, but there is at least one that violates it.
     * INCONCLUSIVE: if none of the above.  
