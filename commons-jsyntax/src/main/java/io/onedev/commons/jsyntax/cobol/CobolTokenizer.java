package io.onedev.commons.jsyntax.cobol;

import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

import io.onedev.commons.jsyntax.AbstractTokenizer;
import io.onedev.commons.jsyntax.StringStream;

public class CobolTokenizer extends AbstractTokenizer<CobolTokenizer.State> {

	static final String BUILTIN = "builtin", COMMENT = "comment", STRING = "string",
		      ATOM = "atom", NUMBER = "number", KEYWORD = "keyword", MODTAG = "header",
		      COBOLLINENUM = "def", PERIOD = "link";
	static final Set<String> atoms = makeKeywords("TRUE FALSE ZEROES ZEROS ZERO SPACES SPACE LOW-VALUE LOW-VALUES");
	static final Set<String> keywords = makeKeywords(
		      		  "ACCEPT ACCESS ACQUIRE ADD ADDRESS " +
		    	      "ADVANCING AFTER ALIAS ALL ALPHABET " +
		    	      "ALPHABETIC ALPHABETIC-LOWER ALPHABETIC-UPPER ALPHANUMERIC ALPHANUMERIC-EDITED " +
		    	      "ALSO ALTER ALTERNATE AND ANY " +
		    	      "ARE AREA AREAS ARITHMETIC ASCENDING " +
		    	      "ASSIGN AT ATTRIBUTE AUTHOR AUTO " +
		    	      "AUTO-SKIP AUTOMATIC B-AND B-EXOR B-LESS " +
		    	      "B-NOT B-OR BACKGROUND-COLOR BACKGROUND-COLOUR BEEP " +
		    	      "BEFORE BELL BINARY BIT BITS " +
		    	      "BLANK BLINK BLOCK BOOLEAN BOTTOM " +
		    	      "BY CALL CANCEL CD CF " +
		    	      "CH CHARACTER CHARACTERS CLASS CLOCK-UNITS " +
		    	      "CLOSE COBOL CODE CODE-SET COL " +
		    	      "COLLATING COLUMN COMMA COMMIT COMMITMENT " +
		    	      "COMMON COMMUNICATION COMP COMP-0 COMP-1 " +
		    	      "COMP-2 COMP-3 COMP-4 COMP-5 COMP-6 " +
		    	      "COMP-7 COMP-8 COMP-9 COMPUTATIONAL COMPUTATIONAL-0 " +
		    	      "COMPUTATIONAL-1 COMPUTATIONAL-2 COMPUTATIONAL-3 COMPUTATIONAL-4 COMPUTATIONAL-5 " +
		    	      "COMPUTATIONAL-6 COMPUTATIONAL-7 COMPUTATIONAL-8 COMPUTATIONAL-9 COMPUTE " +
		    	      "CONFIGURATION CONNECT CONSOLE CONTAINED CONTAINS " +
		    	      "CONTENT CONTINUE CONTROL CONTROL-AREA CONTROLS " +
		    	      "CONVERTING COPY CORR CORRESPONDING COUNT " +
		    	      "CRT CRT-UNDER CURRENCY CURRENT CURSOR " +
		    	      "DATA DATE DATE-COMPILED DATE-WRITTEN DAY " +
		    	      "DAY-OF-WEEK DB DB-ACCESS-CONTROL-KEY DB-DATA-NAME DB-EXCEPTION " +
		    	      "DB-FORMAT-NAME DB-RECORD-NAME DB-SET-NAME DB-STATUS DBCS " +
		    	      "DBCS-EDITED DE DEBUG-CONTENTS DEBUG-ITEM DEBUG-LINE " +
		    	      "DEBUG-NAME DEBUG-SUB-1 DEBUG-SUB-2 DEBUG-SUB-3 DEBUGGING " +
		    	      "DECIMAL-POINT DECLARATIVES DEFAULT DELETE DELIMITED " +
		    	      "DELIMITER DEPENDING DESCENDING DESCRIBED DESTINATION " +
		    	      "DETAIL DISABLE DISCONNECT DISPLAY DISPLAY-1 " +
		    	      "DISPLAY-2 DISPLAY-3 DISPLAY-4 DISPLAY-5 DISPLAY-6 " +
		    	      "DISPLAY-7 DISPLAY-8 DISPLAY-9 DIVIDE DIVISION " +
		    	      "DOWN DROP DUPLICATE DUPLICATES DYNAMIC " +
		    	      "EBCDIC EGI EJECT ELSE EMI " +
		    	      "EMPTY EMPTY-CHECK ENABLE END END. END-ACCEPT END-ACCEPT. " +
		    	      "END-ADD END-CALL END-COMPUTE END-DELETE END-DISPLAY " +
		    	      "END-DIVIDE END-EVALUATE END-IF END-INVOKE END-MULTIPLY " +
		    	      "END-OF-PAGE END-PERFORM END-READ END-RECEIVE END-RETURN " +
		    	      "END-REWRITE END-SEARCH END-START END-STRING END-SUBTRACT " +
		    	      "END-UNSTRING END-WRITE END-XML ENTER ENTRY " +
		    	      "ENVIRONMENT EOP EQUAL EQUALS ERASE " +
		    	      "ERROR ESI EVALUATE EVERY EXCEEDS " +
		    	      "EXCEPTION EXCLUSIVE EXIT EXTEND EXTERNAL " +
		    	      "EXTERNALLY-DESCRIBED-KEY FD FETCH FILE FILE-CONTROL " +
		    	      "FILE-STREAM FILES FILLER FINAL FIND " +
		    	      "FINISH FIRST FOOTING FOR FOREGROUND-COLOR " +
		    	      "FOREGROUND-COLOUR FORMAT FREE FROM FULL " +
		    	      "FUNCTION GENERATE GET GIVING GLOBAL " +
		    	      "GO GOBACK GREATER GROUP HEADING " +
		    	      "HIGH-VALUE HIGH-VALUES HIGHLIGHT I-O I-O-CONTROL " +
		    	      "ID IDENTIFICATION IF IN INDEX " +
		    	      "INDEX-1 INDEX-2 INDEX-3 INDEX-4 INDEX-5 " +
		    	      "INDEX-6 INDEX-7 INDEX-8 INDEX-9 INDEXED " +
		    	      "INDIC INDICATE INDICATOR INDICATORS INITIAL " +
		    	      "INITIALIZE INITIATE INPUT INPUT-OUTPUT INSPECT " +
		    	      "INSTALLATION INTO INVALID INVOKE IS " +
		    	      "JUST JUSTIFIED KANJI KEEP KEY " +
		    	      "LABEL LAST LD LEADING LEFT " +
		    	      "LEFT-JUSTIFY LENGTH LENGTH-CHECK LESS LIBRARY " +
		    	      "LIKE LIMIT LIMITS LINAGE LINAGE-COUNTER " +
		    	      "LINE LINE-COUNTER LINES LINKAGE LOCAL-STORAGE " +
		    	      "LOCALE LOCALLY LOCK " +
		    	      "MEMBER MEMORY MERGE MESSAGE METACLASS " +
		    	      "MODE MODIFIED MODIFY MODULES MOVE " +
		    	      "MULTIPLE MULTIPLY NATIONAL NATIVE NEGATIVE " +
		    	      "NEXT NO NO-ECHO NONE NOT " +
		    	      "NULL NULL-KEY-MAP NULL-MAP NULLS NUMBER " +
		    	      "NUMERIC NUMERIC-EDITED OBJECT OBJECT-COMPUTER OCCURS " +
		    	      "OF OFF OMITTED ON ONLY " +
		    	      "OPEN OPTIONAL OR ORDER ORGANIZATION " +
		    	      "OTHER OUTPUT OVERFLOW OWNER PACKED-DECIMAL " +
		    	      "PADDING PAGE PAGE-COUNTER PARSE PERFORM " +
		    	      "PF PH PIC PICTURE PLUS " +
		    	      "POINTER POSITION POSITIVE PREFIX PRESENT " +
		    	      "PRINTING PRIOR PROCEDURE PROCEDURE-POINTER PROCEDURES " +
		    	      "PROCEED PROCESS PROCESSING PROGRAM PROGRAM-ID " +
		    	      "PROMPT PROTECTED PURGE QUEUE QUOTE " +
		    	      "QUOTES RANDOM RD READ READY " +
		    	      "REALM RECEIVE RECONNECT RECORD RECORD-NAME " +
		    	      "RECORDS RECURSIVE REDEFINES REEL REFERENCE " +
		    	      "REFERENCE-MONITOR REFERENCES RELATION RELATIVE RELEASE " +
		    	      "REMAINDER REMOVAL RENAMES REPEATED REPLACE " +
		    	      "REPLACING REPORT REPORTING REPORTS REPOSITORY " +
		    	      "REQUIRED RERUN RESERVE RESET RETAINING " +
		    	      "RETRIEVAL RETURN RETURN-CODE RETURNING REVERSE-VIDEO " +
		    	      "REVERSED REWIND REWRITE RF RH " +
		    	      "RIGHT RIGHT-JUSTIFY ROLLBACK ROLLING ROUNDED " +
		    	      "RUN SAME SCREEN SD SEARCH " +
		    	      "SECTION SECURE SECURITY SEGMENT SEGMENT-LIMIT " +
		    	      "SELECT SEND SENTENCE SEPARATE SEQUENCE " +
		    	      "SEQUENTIAL SET SHARED SIGN SIZE " +
		    	      "SKIP1 SKIP2 SKIP3 SORT SORT-MERGE " +
		    	      "SORT-RETURN SOURCE SOURCE-COMPUTER SPACE-FILL " +
		    	      "SPECIAL-NAMES STANDARD STANDARD-1 STANDARD-2 " +
		    	      "START STARTING STATUS STOP STORE " +
		    	      "STRING SUB-QUEUE-1 SUB-QUEUE-2 SUB-QUEUE-3 SUB-SCHEMA " +
		    	      "SUBFILE SUBSTITUTE SUBTRACT SUM SUPPRESS " +
		    	      "SYMBOLIC SYNC SYNCHRONIZED SYSIN SYSOUT " +
		    	      "TABLE TALLYING TAPE TENANT TERMINAL " +
		    	      "TERMINATE TEST TEXT THAN THEN " +
		    	      "THROUGH THRU TIME TIMES TITLE " +
		    	      "TO TOP TRAILING TRAILING-SIGN TRANSACTION " +
		    	      "TYPE TYPEDEF UNDERLINE UNEQUAL UNIT " +
		    	      "UNSTRING UNTIL UP UPDATE UPON " +
		    	      "USAGE USAGE-MODE USE USING VALID " +
		    	      "VALIDATE VALUE VALUES VARYING VLR " +
		    	      "WAIT WHEN WHEN-COMPILED WITH WITHIN " +
		    	      "WORDS WORKING-STORAGE WRITE XML XML-CODE " +
		    	      "XML-EVENT XML-NTEXT XML-TEXT ZERO ZERO-FILL " );
	static final Set<String> builtins = makeKeywords("- * ** / + < <= = > >= ");
	
	static final Tests tests = new Tests();
	
	static Set<String> makeKeywords(String str) {
		Set<String> s = Sets.newHashSet();
		String[] arr = str.split(" ");
		for (String o : arr) {
			s.add(o);
		}
		return s;
	}

	static class State {
	    String mode;

	    public State(String mode) {
	        this.mode = mode;
	    }
	}
	
	static class Tests {
		Pattern digit, digit_or_colon, hex, sign, exponent, keyword_char, symbol;
		
		public Tests() {
			this.digit = Pattern.compile("\\d");
			this.digit_or_colon = Pattern.compile("[\\d:]");
			this.hex = Pattern.compile("[0-9a-f]", Pattern.CASE_INSENSITIVE);
			this.sign = Pattern.compile("[+-]");
			this.exponent = Pattern.compile("e", Pattern.CASE_INSENSITIVE);
			this.keyword_char = Pattern.compile("[^\\s\\(\\[\\;\\)\\]]");
			this.symbol = Pattern.compile("[\\w*+\\-]");
		}
	}
	
	static final Pattern pattern = Pattern.compile("x", Pattern.CASE_INSENSITIVE);
	
	boolean isNumber(String ch, StringStream stream) {
	    if (ch.equals("0") && !stream.eat(pattern).isEmpty()) {
	        stream.eatWhile(tests.hex);
	        return true;
	    }
	    if ((ch.equals("+") || ch.equals("-")) && (tests.digit.matcher(stream.peek()).matches())) {
	        stream.eat(tests.sign);
	        ch = stream.next();
	    }
	    if (tests.digit.matcher(ch).matches()) {
	        stream.eat(ch);
	        stream.eatWhile(tests.digit);
	        if (stream.peek().equals(".")) {
	            stream.eat(".");
	            stream.eatWhile(tests.digit);
	        }
	        if (!stream.eat(tests.exponent).isEmpty()) {
	            stream.eat(tests.sign);
	            stream.eatWhile(tests.digit);
	        }
	        return true;
	    }
	    return false;
	}
	
	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "cpy", "cob", "cbl", "pco");
	}

	@Override
	public State startState() {
		return new State("");
	}

	@Override
	public String token(StringStream stream, State state) {
	    if (stream.eatSpace()) {
	        return "";
	    }
	    String returnType = "";
	    switch(state.mode) {
	        case "string" :
	        	String next = "";
		        while (!(next = stream.next()).isEmpty()) {
		            if (next.equals("\"") || next.equals("\'")) {
		                state.mode = "";
		                break;
		            }
		        }
		        returnType = STRING;
		        break;
	        default :
	        	String ch = stream.next();
		        int col = stream.column();
		        if (col >= 0 && col <= 5) {
		            returnType = COBOLLINENUM;
		        }
		        else if (col >= 72 && col <= 79) {
		            stream.skipToEnd();
		            returnType = MODTAG;
		        }
		        else if (ch.equals("*") && col == 6) {
		            stream.skipToEnd();
		            returnType = COMMENT;
		        }
		        else if (ch.equals("\"") || ch.equals("\'")) {
		            state.mode = "string";
		            returnType = STRING;
		        }
		        else if (ch.equals("'") && !(tests.digit_or_colon.matcher(stream.peek()).matches())) {
		            returnType = ATOM;
		        }
		        else if (ch.equals(".")) {
		            returnType = PERIOD;
		        }
		        else if (isNumber(ch, stream)) {
		            returnType = NUMBER;
		        }
		        else {
		            if (tests.symbol.matcher(stream.current()).matches()) {
		                while (col < 71) {
		                    if (stream.eat(tests.symbol).isEmpty()) {
		                        break;
		                    }
		                    else {
		                        col++;
		                    }
		                }
		            }
		            if (keywords.contains(stream.current().toUpperCase())) {
		                returnType = KEYWORD;
		            }
		            else if (builtins.contains(stream.current().toUpperCase())) {
		                returnType = BUILTIN;
		            }
		            else if (atoms.contains(stream.current().toUpperCase())) {
		                returnType = ATOM;
		            }
		            else returnType = "";
		        }
	    }
	    return returnType;
	}

	@Override
	public boolean acceptMime(String mime) {
		return mime != null && mime.equals("text/x-cobol");
	}

	@Override
	public boolean acceptMode(String mode) {
		return mode != null && mode.equals("cobol");
	}
}
