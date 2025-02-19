// Generated by JFlex 1.9.1 http://jflex.de/  (tweaked for IntelliJ platform)
// source: _HttpLexer.flex

package org.javamaster.httpclient;

import com.intellij.psi.tree.IElementType;
import static org.javamaster.httpclient.utils.LexerUtils.*;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.javamaster.httpclient.psi.HttpTypes.*;



public class _HttpLexer implements com.intellij.lexer.FlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int IN_GLOBAL_SCRIPT = 2;
  public static final int IN_GLOBAL_SCRIPT_END = 4;
  public static final int IN_PRE_SCRIPT = 6;
  public static final int IN_PRE_SCRIPT_END = 8;
  public static final int IN_DIRECTION_COMMENT = 10;
  public static final int IN_FIRST_LINE = 12;
  public static final int IN_HOST = 14;
  public static final int IN_PORT = 16;
  public static final int IN_PATH = 18;
  public static final int IN_QUERY = 20;
  public static final int IN_FRAGMENT = 22;
  public static final int IN_BODY = 24;
  public static final int IN_TRIM_PREFIX_SPACE = 26;
  public static final int IN_HEADER = 28;
  public static final int IN_HEADER_FIELD_NAME = 30;
  public static final int IN_HEADER_FIELD_VALUE = 32;
  public static final int IN_HEADER_FIELD_VALUE_NO_SPACE = 34;
  public static final int IN_POST_SCRIPT = 36;
  public static final int IN_POST_SCRIPT_END = 38;
  public static final int IN_RES_SCRIPT_BODY_PAET = 40;
  public static final int IN_INPUT_FILE_PATH = 42;
  public static final int IN_OUTPUT_FILE = 44;
  public static final int IN_OUTPUT_FILE_PATH = 46;
  public static final int IN_VERSION = 48;
  public static final int IN_MULTIPART = 50;
  public static final int IN_VARIABLE = 52;
  public static final int IN_DINAMIC_VARIABLE = 54;
  public static final int IN_GLOBAL_VARIABLE = 56;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = {
     0,  0,  1,  1,  2,  2,  3,  3,  4,  4,  5,  5,  6,  6,  7,  7, 
     8,  8,  9,  9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14, 15, 15, 
    16, 16, 17, 17, 18, 18, 19, 19, 20, 20, 21, 21, 22, 22, 23, 23, 
    24, 24, 25, 25, 26, 26, 27, 27, 28, 28
  };

  /**
   * Top-level table for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_TOP = zzUnpackcmap_top();

  private static final String ZZ_CMAP_TOP_PACKED_0 =
    "\1\0\25\u0100\1\u0200\11\u0100\1\u0300\17\u0100\1\u0400\247\u0100"+
    "\10\u0500\u1020\u0100";

  private static int [] zzUnpackcmap_top() {
    int [] result = new int[4352];
    int offset = 0;
    offset = zzUnpackcmap_top(ZZ_CMAP_TOP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_top(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Second-level tables for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_BLOCKS = zzUnpackcmap_blocks();

  private static final String ZZ_CMAP_BLOCKS_PACKED_0 =
    "\11\0\1\1\1\2\2\3\1\4\22\0\1\5\1\6"+
    "\1\0\1\7\1\10\1\11\1\12\6\0\1\13\1\14"+
    "\1\15\12\16\1\17\1\0\1\20\1\21\1\22\1\23"+
    "\1\24\7\25\1\26\7\25\1\27\3\25\1\30\6\25"+
    "\4\0\1\31\1\0\1\32\1\33\1\32\1\34\3\32"+
    "\1\35\6\32\1\36\1\37\2\32\1\40\1\41\1\42"+
    "\1\32\1\43\3\32\1\44\1\0\1\45\7\0\1\3"+
    "\32\0\1\1\u01df\0\1\1\177\0\13\1\35\0\2\3"+
    "\5\0\1\1\57\0\1\1\240\0\1\1\377\0\u0100\46";

  private static int [] zzUnpackcmap_blocks() {
    int [] result = new int[1536];
    int offset = 0;
    offset = zzUnpackcmap_blocks(ZZ_CMAP_BLOCKS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_blocks(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /**
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\15\0\1\1\17\0\1\2\1\1\3\2\1\3\1\4"+
    "\4\5\1\6\1\2\1\5\1\2\2\7\2\6\1\10"+
    "\1\7\2\11\1\1\5\2\1\12\1\13\1\14\1\15"+
    "\1\16\1\17\1\20\1\21\1\22\1\2\1\23\1\24"+
    "\1\25\1\2\2\26\12\27\2\30\2\31\2\32\1\31"+
    "\1\33\1\34\4\35\2\5\2\36\5\5\1\2\2\37"+
    "\1\32\1\37\1\1\1\37\3\40\2\41\1\6\1\41"+
    "\1\1\1\41\3\2\1\42\1\43\1\2\4\44\1\2"+
    "\6\0\1\45\1\5\1\0\1\46\1\0\1\7\3\0"+
    "\1\47\1\50\1\51\1\52\1\26\1\27\5\0\1\27"+
    "\1\53\2\0\1\54\1\5\1\36\1\55\1\5\1\56"+
    "\1\37\1\0\1\41\3\0\1\57\1\44\1\60\1\61"+
    "\1\0\2\62\2\0\1\63\1\64\2\0\1\47\1\0"+
    "\1\65\1\0\1\66\1\5\1\67\1\70\1\0\1\71"+
    "\2\72\3\0\2\73\1\65\3\0\1\71\1\74\1\0"+
    "\1\75\1\76\1\0\1\77\2\0\1\100";

  private static int [] zzUnpackAction() {
    int [] result = new int[219];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\47\0\116\0\165\0\234\0\303\0\352\0\u0111"+
    "\0\u0138\0\u015f\0\u0186\0\u01ad\0\u01d4\0\u01fb\0\u0222\0\u0249"+
    "\0\u0270\0\u0297\0\u02be\0\u02e5\0\u030c\0\u0333\0\u035a\0\u0381"+
    "\0\u03a8\0\u03cf\0\u03f6\0\u041d\0\u0444\0\u046b\0\u0492\0\u04b9"+
    "\0\u04e0\0\u0507\0\u046b\0\u052e\0\u0555\0\u057c\0\u05a3\0\u05ca"+
    "\0\u05f1\0\u0618\0\u063f\0\u0666\0\u068d\0\u06b4\0\u0666\0\u06db"+
    "\0\u0702\0\u0729\0\u0666\0\u0750\0\u0777\0\u079e\0\u07c5\0\u07ec"+
    "\0\u0813\0\u083a\0\u0861\0\u046b\0\u0888\0\u04e0\0\u046b\0\u046b"+
    "\0\u08af\0\u08d6\0\u04e0\0\u08fd\0\u0924\0\u046b\0\u046b\0\u094b"+
    "\0\u0972\0\u0999\0\u09c0\0\u09e7\0\u0a0e\0\u0a35\0\u0a5c\0\u0a83"+
    "\0\u0aaa\0\u0ad1\0\u0af8\0\u0b1f\0\u046b\0\u04e0\0\u046b\0\u0666"+
    "\0\u0666\0\u0b46\0\u04e0\0\u0b6d\0\u046b\0\u046b\0\u0666\0\u04e0"+
    "\0\u0b94\0\u0bbb\0\u0be2\0\u0666\0\u0c09\0\u0c30\0\u0c57\0\u0c7e"+
    "\0\u0ca5\0\u0ccc\0\u0cf3\0\u0d1a\0\u0d41\0\u0d68\0\u0d8f\0\u0db6"+
    "\0\u0ddd\0\u046b\0\u04e0\0\u0e04\0\u0e2b\0\u0e52\0\u0e79\0\u0ea0"+
    "\0\u0ec7\0\u0eee\0\u0f15\0\u0f3c\0\u0f63\0\u046b\0\u0f8a\0\u0fb1"+
    "\0\u0fd8\0\u0fff\0\u1026\0\u104d\0\u1074\0\u04e0\0\u109b\0\u10c2"+
    "\0\u10e9\0\u1110\0\u1137\0\u046b\0\u115e\0\u1185\0\u046b\0\u0666"+
    "\0\u11ac\0\u11d3\0\u11fa\0\u1221\0\u1248\0\u046b\0\u046b\0\u046b"+
    "\0\u126f\0\u1296\0\u0a83\0\u0aaa\0\u0b1f\0\u12bd\0\u12e4\0\u130b"+
    "\0\u046b\0\u1332\0\u1359\0\u046b\0\u1380\0\u046b\0\u13a7\0\u13ce"+
    "\0\u13f5\0\u141c\0\u1443\0\u146a\0\u1491\0\u14b8\0\u14df\0\u046b"+
    "\0\u1506\0\u046b\0\u046b\0\u152d\0\u046b\0\u1554\0\u157b\0\u15a2"+
    "\0\u15c9\0\u046b\0\u15f0\0\u1617\0\u046b\0\u163e\0\u1665\0\u168c"+
    "\0\u046b\0\u16b3\0\u046b\0\u046b\0\u16da\0\u1701\0\u046b\0\u1728"+
    "\0\u174f\0\u1776\0\u179d\0\u046b\0\u17c4\0\u17eb\0\u1812\0\u1839"+
    "\0\u1860\0\u1887\0\u18ae\0\u18d5\0\u18fc\0\u1923\0\u194a\0\u1971"+
    "\0\u1998\0\u19bf\0\u19bf";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[219];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length() - 1;
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /**
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpacktrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\36\5\37\1\36\1\40\5\36\1\41\2\36\1\42"+
    "\3\36\1\43\4\44\16\36\1\45\5\46\3\45\1\47"+
    "\3\45\1\50\31\45\1\36\5\51\3\36\1\52\3\36"+
    "\1\41\31\36\1\45\5\46\3\45\1\53\3\45\1\50"+
    "\31\45\1\36\5\54\3\36\1\52\3\36\1\41\31\36"+
    "\1\55\1\56\1\57\1\56\1\60\1\61\7\55\1\62"+
    "\31\55\1\36\1\54\2\63\1\64\1\65\7\36\1\41"+
    "\1\36\1\66\14\36\1\67\1\70\5\36\1\71\1\72"+
    "\3\36\1\54\2\63\1\64\1\73\1\36\1\74\3\36"+
    "\2\75\1\76\1\75\1\77\3\36\1\100\1\36\4\75"+
    "\1\36\12\75\1\72\3\36\1\54\2\63\1\64\1\73"+
    "\7\36\1\41\1\101\30\36\1\102\1\54\2\63\1\64"+
    "\1\73\1\102\1\74\5\102\1\103\5\102\1\100\20\102"+
    "\1\104\2\102\1\105\1\54\2\63\1\64\1\73\1\105"+
    "\1\74\2\105\1\106\2\105\1\41\3\105\1\107\22\105"+
    "\1\110\2\105\1\111\1\54\2\63\1\64\1\73\7\111"+
    "\1\112\31\111\1\113\1\114\1\115\1\116\1\115\1\117"+
    "\1\113\1\120\3\113\1\121\1\113\1\122\2\113\1\123"+
    "\1\113\1\124\24\113\1\125\5\37\7\125\1\126\31\125"+
    "\1\127\1\130\1\131\1\130\1\132\1\130\7\127\1\133"+
    "\31\127\1\36\4\54\1\65\5\36\1\134\1\36\1\41"+
    "\1\134\1\135\5\36\4\134\1\36\12\134\3\36\1\136"+
    "\1\137\1\63\1\137\1\64\1\65\7\136\1\140\26\136"+
    "\1\141\2\136\1\142\1\143\1\144\1\143\1\145\1\146"+
    "\7\142\1\147\26\142\1\150\2\142\1\45\5\46\3\45"+
    "\1\151\3\45\1\50\4\45\1\152\24\45\1\36\5\54"+
    "\3\36\1\153\3\36\1\41\32\36\5\54\7\36\1\41"+
    "\31\36\1\154\1\155\1\156\1\157\1\156\1\160\7\154"+
    "\1\161\2\154\1\36\1\154\1\36\24\154\1\162\5\51"+
    "\7\162\1\163\4\162\1\164\24\162\1\165\1\166\1\167"+
    "\1\170\1\167\1\171\7\165\1\172\2\165\1\36\1\165"+
    "\1\173\24\165\1\36\1\54\2\63\1\64\1\65\7\36"+
    "\1\41\10\36\1\174\21\36\5\54\5\36\1\175\1\36"+
    "\1\41\32\36\4\54\1\65\2\36\1\176\2\36\1\177"+
    "\1\36\1\41\1\177\6\36\17\177\1\36\1\200\2\36"+
    "\4\54\1\65\5\36\1\177\1\36\1\41\1\177\6\36"+
    "\17\177\1\36\1\200\1\36\1\201\1\202\1\167\1\203"+
    "\1\167\1\171\7\201\1\204\3\201\1\107\22\201\1\205"+
    "\1\36\1\201\50\0\5\37\7\0\1\206\36\0\1\207"+
    "\1\0\1\210\54\0\1\211\36\0\1\212\1\213\65\0"+
    "\4\44\16\0\11\45\1\0\36\45\5\46\3\45\1\0"+
    "\3\45\1\50\31\45\45\0\1\214\1\0\11\45\1\0"+
    "\3\45\1\215\31\45\1\0\5\51\7\0\1\206\76\0"+
    "\1\216\46\0\1\217\2\0\5\220\7\0\1\206\31\0"+
    "\2\55\1\0\1\55\2\0\42\55\1\56\1\220\1\56"+
    "\2\220\7\55\1\62\31\55\1\0\1\220\1\57\3\220"+
    "\7\0\1\206\32\0\4\220\1\61\7\0\1\206\31\0"+
    "\2\55\1\0\1\55\2\0\7\55\1\221\31\55\1\0"+
    "\1\220\1\63\3\220\7\0\1\206\32\0\4\220\1\65"+
    "\7\0\1\206\46\0\1\222\73\0\1\223\45\0\1\224"+
    "\45\0\1\225\52\0\1\226\3\0\4\220\1\73\7\0"+
    "\1\206\44\0\2\75\1\0\1\75\6\0\4\75\1\0"+
    "\12\75\21\0\1\101\30\0\1\102\5\0\1\102\1\0"+
    "\5\102\1\0\5\102\1\0\20\102\1\0\2\102\44\0"+
    "\1\227\2\0\1\105\5\0\1\105\1\0\2\105\1\0"+
    "\2\105\1\0\3\105\1\0\22\105\1\0\2\105\44\0"+
    "\1\230\2\0\1\111\5\0\42\111\5\0\7\111\1\231"+
    "\31\111\2\113\1\0\1\113\1\0\2\113\1\0\3\113"+
    "\1\0\4\113\1\0\1\113\1\0\25\113\1\114\1\232"+
    "\1\114\1\232\1\114\1\113\1\0\3\113\1\0\1\113"+
    "\1\122\2\113\1\0\1\113\1\0\24\113\1\0\1\232"+
    "\3\115\1\232\1\0\1\233\3\0\1\234\1\0\1\206"+
    "\4\0\1\235\24\0\1\113\1\114\1\115\1\116\1\115"+
    "\1\114\1\113\1\233\3\113\1\234\1\113\1\122\2\113"+
    "\1\0\1\113\1\235\25\113\1\114\1\115\1\116\1\115"+
    "\1\117\1\113\1\0\3\113\1\0\1\113\1\122\2\113"+
    "\1\0\1\113\1\0\24\113\7\0\1\236\52\0\1\237"+
    "\33\0\2\113\1\0\1\113\1\0\2\113\1\0\3\113"+
    "\1\0\1\113\1\240\2\113\1\0\1\113\1\0\24\113"+
    "\5\0\1\241\46\0\1\242\14\0\1\243\25\0\1\220"+
    "\1\131\3\220\7\0\1\206\44\0\1\134\2\0\1\134"+
    "\6\0\4\134\1\0\12\134\47\0\1\244\2\0\2\142"+
    "\1\0\1\142\2\0\36\142\1\0\3\142\1\143\1\220"+
    "\1\143\2\220\7\142\1\147\26\142\1\0\2\142\1\0"+
    "\1\220\1\144\3\220\7\0\1\206\32\0\1\220\2\144"+
    "\1\145\1\146\7\0\1\206\31\0\2\142\1\0\1\142"+
    "\2\0\7\142\1\245\26\142\1\0\2\142\44\0\1\246"+
    "\47\0\1\247\1\0\5\45\1\250\3\45\1\0\35\45"+
    "\45\0\1\251\1\0\2\154\1\0\1\154\2\0\12\154"+
    "\1\0\1\154\1\0\25\154\1\155\1\220\1\155\2\220"+
    "\7\154\1\161\2\154\1\0\1\154\1\0\24\154\1\0"+
    "\1\220\3\156\1\220\7\0\1\206\31\0\1\154\1\155"+
    "\1\156\1\157\1\156\1\220\7\154\1\161\2\154\1\0"+
    "\1\154\1\0\24\154\1\0\1\220\3\156\1\160\7\0"+
    "\1\206\31\0\2\154\1\0\1\154\2\0\7\154\1\252"+
    "\2\154\1\0\1\154\1\0\24\154\22\0\1\253\24\0"+
    "\2\165\1\0\1\165\2\0\12\165\1\0\1\165\1\0"+
    "\25\165\1\166\1\220\1\166\2\220\7\165\1\172\2\165"+
    "\1\0\1\165\1\0\24\165\1\0\1\220\3\167\1\220"+
    "\7\0\1\206\31\0\1\165\1\166\1\167\1\170\1\167"+
    "\1\220\7\165\1\172\2\165\1\0\1\165\1\0\24\165"+
    "\1\0\1\220\3\167\1\171\7\0\1\206\31\0\2\165"+
    "\1\0\1\165\2\0\7\165\1\254\2\165\1\0\1\165"+
    "\1\0\24\165\22\0\1\255\54\0\1\256\31\0\1\257"+
    "\46\0\1\177\2\0\1\177\6\0\17\177\50\0\1\260"+
    "\1\0\2\201\1\0\1\201\2\0\13\201\1\0\22\201"+
    "\2\0\2\201\1\202\1\220\1\202\2\220\7\201\1\204"+
    "\3\201\1\0\22\201\2\0\2\201\1\202\1\167\1\203"+
    "\1\167\1\220\7\201\1\204\3\201\1\0\22\201\2\0"+
    "\3\201\1\0\1\201\2\0\7\201\1\261\3\201\1\0"+
    "\22\201\2\0\1\201\44\0\1\262\26\0\1\263\31\0"+
    "\1\264\37\0\2\211\2\265\1\266\41\211\45\0\1\267"+
    "\7\0\1\270\41\0\2\215\3\45\4\215\1\211\34\215"+
    "\1\45\2\0\3\271\1\216\41\0\2\221\1\265\1\55"+
    "\1\266\1\211\40\221\1\55\15\0\1\272\64\0\1\273"+
    "\54\0\1\274\45\0\1\275\6\0\1\231\1\211\2\265"+
    "\1\266\1\211\40\231\1\111\1\0\5\232\7\0\1\206"+
    "\40\0\1\276\52\0\1\277\2\0\1\277\6\0\4\277"+
    "\1\0\12\277\3\0\2\240\1\265\1\113\1\266\2\240"+
    "\1\211\3\240\1\211\4\240\1\211\1\240\1\211\23\240"+
    "\1\113\44\0\1\300\7\0\1\301\41\0\2\245\1\265"+
    "\1\142\1\266\1\211\36\245\1\211\1\245\1\142\1\0"+
    "\5\247\41\0\11\45\1\0\32\45\1\302\2\45\1\0"+
    "\5\251\41\0\2\252\1\265\1\154\1\266\1\211\12\252"+
    "\1\211\1\252\1\211\23\252\1\154\5\0\1\303\41\0"+
    "\2\254\1\265\1\165\1\266\1\211\12\254\1\211\1\254"+
    "\1\211\23\254\1\165\5\0\1\304\71\0\1\305\31\0"+
    "\1\306\2\0\1\306\6\0\4\306\1\0\12\306\3\0"+
    "\2\261\1\265\1\201\1\266\1\211\13\261\1\211\22\261"+
    "\2\211\1\201\2\264\2\307\1\310\41\264\3\0\1\265"+
    "\55\0\1\311\101\0\1\312\4\0\3\271\75\0\1\313"+
    "\52\0\1\225\7\0\2\276\2\314\1\315\41\276\2\0"+
    "\5\316\5\0\1\277\2\0\1\277\6\0\4\277\1\0"+
    "\12\277\14\0\1\317\35\0\11\45\1\320\35\45\27\0"+
    "\1\321\20\0\5\322\5\0\1\306\2\0\1\306\6\0"+
    "\4\306\1\0\12\306\5\0\1\307\46\0\3\323\1\311"+
    "\52\0\1\324\73\0\1\275\12\0\1\314\45\0\5\316"+
    "\43\0\3\325\1\317\43\0\3\326\1\320\56\0\1\327"+
    "\32\0\5\322\43\0\3\323\44\0\3\330\1\324\43\0"+
    "\3\325\44\0\3\326\60\0\1\331\32\0\3\330\56\0"+
    "\1\332\1\0\1\331\46\0\1\333\30\0";

  private static int [] zzUnpacktrans() {
    int [] result = new int[6630];
    int offset = 0;
    offset = zzUnpacktrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpacktrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String[] ZZ_ERROR_MSG = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state {@code aState}
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\15\0\1\1\17\0\1\11\4\1\1\11\30\1\1\11"+
    "\2\1\2\11\5\1\2\11\15\1\1\11\1\1\1\11"+
    "\5\1\2\11\23\1\1\11\13\1\1\11\7\1\6\0"+
    "\1\11\1\1\1\0\1\11\1\0\1\1\3\0\1\1"+
    "\3\11\2\1\5\0\1\1\1\11\2\0\1\11\1\1"+
    "\1\11\4\1\1\0\1\1\3\0\1\11\1\1\2\11"+
    "\1\0\1\11\1\1\2\0\1\1\1\11\2\0\1\11"+
    "\1\0\1\1\1\0\1\11\1\1\2\11\1\0\1\1"+
    "\1\11\1\1\3\0\1\11\2\1\3\0\2\1\1\0"+
    "\2\1\1\0\1\1\2\0\1\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[219];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private CharSequence zzBuffer = "";

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** Number of newlines encountered up to the start of the matched text. */
  @SuppressWarnings("unused")
  private int yyline;

  /** Number of characters from the last newline up to the start of the matched text. */
  @SuppressWarnings("unused")
  protected int yycolumn;

  /** Number of characters up to the start of the matched text. */
  @SuppressWarnings("unused")
  private long yychar;

  /** Whether the scanner is currently at the beginning of a line. */
  @SuppressWarnings("unused")
  private boolean zzAtBOL = true;

  /** Whether the user-EOF-code has already been executed. */
  @SuppressWarnings("unused")
  private boolean zzEOFDone;

  /* user code: */
        private boolean nameFlag = true;
        int nextState;
        public int matchTimes = 0;

        public _HttpLexer() {
          this((java.io.Reader)null);
        }

        private static String zzToPrintable(CharSequence str) {
          return zzToPrintable(str.toString());
        }


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public _HttpLexer(java.io.Reader in) {
    this.zzReader = in;
  }


  /** Returns the maximum size of the scanner buffer, which limits the size of tokens. */
  private int zzMaxBufferLen() {
    return Integer.MAX_VALUE;
  }

  /**  Whether the scanner buffer can grow to accommodate a larger token. */
  private boolean zzCanGrow() {
    return true;
  }

  /**
   * Translates raw input code points to DFA table row
   */
  private static int zzCMap(int input) {
    int offset = input & 255;
    return offset == input ? ZZ_CMAP_BLOCKS[offset] : ZZ_CMAP_BLOCKS[ZZ_CMAP_TOP[input >> 8] | offset];
  }

  public final int getTokenStart() {
    return zzStartRead;
  }

  public final int getTokenEnd() {
    return getTokenStart() + yylength();
  }

  public void reset(CharSequence buffer, int start, int end, int initialState) {
    zzBuffer = buffer;
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
  }

  /**
   * Refills the input buffer.
   *
   * @return      {@code false}, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return zzBuffer.subSequence(zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position {@code pos} from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer.charAt(zzStartRead+pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occurred while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public IElementType advance() throws java.io.IOException
  {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    CharSequence zzBufferL = zzBuffer;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMap(zzInput) ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
            switch (zzLexicalState) {
            case IN_BODY: {
              nextState = YYINITIAL; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this);
            }  // fall though
            case 220: break;
            case IN_POST_SCRIPT: {
              yybegin(YYINITIAL); return SCRIPT_BODY_PAET;
            }  // fall though
            case 221: break;
            default:
        return null;
        }
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1:
            { return WHITE_SPACE;
            }
          // fall through
          case 65: break;
          case 2:
            { return BAD_CHARACTER;
            }
          // fall through
          case 66: break;
          case 3:
            { nameFlag = true; yybegin(IN_GLOBAL_VARIABLE); return AT;
            }
          // fall through
          case 67: break;
          case 4:
            { yybegin(IN_FIRST_LINE); return REQUEST_METHOD;
            }
          // fall through
          case 68: break;
          case 5:
            { 
            }
          // fall through
          case 69: break;
          case 6:
            { yybegin(YYINITIAL); return WHITE_SPACE;
            }
          // fall through
          case 70: break;
          case 7:
            { if(nameFlag) return DIRECTION_NAME_PART; else return DIRECTION_VALUE_PART;
            }
          // fall through
          case 71: break;
          case 8:
            { nameFlag = false; return WHITE_SPACE;
            }
          // fall through
          case 72: break;
          case 9:
            { yybegin(IN_HEADER); return WHITE_SPACE;
            }
          // fall through
          case 73: break;
          case 10:
            { yybegin(IN_VERSION); return WHITE_SPACE;
            }
          // fall through
          case 74: break;
          case 11:
            { yybegin(IN_FRAGMENT); return HASH;
            }
          // fall through
          case 75: break;
          case 12:
            { return HOST_VALUE;
            }
          // fall through
          case 76: break;
          case 13:
            { yybegin(IN_PATH); return SLASH;
            }
          // fall through
          case 77: break;
          case 14:
            { yybegin(IN_PORT); return COLON;
            }
          // fall through
          case 78: break;
          case 15:
            { nameFlag = true; yybegin(IN_QUERY); return QUESTION;
            }
          // fall through
          case 79: break;
          case 16:
            { yybegin(IN_PATH); return PORT_SEGMENT;
            }
          // fall through
          case 80: break;
          case 17:
            { return SEGMENT;
            }
          // fall through
          case 81: break;
          case 18:
            { return SLASH;
            }
          // fall through
          case 82: break;
          case 19:
            { if(nameFlag) return QUERY_NAME; else return QUERY_VALUE;
            }
          // fall through
          case 83: break;
          case 20:
            { nameFlag = true; return AND;
            }
          // fall through
          case 84: break;
          case 21:
            { nameFlag = false; return EQUALS;
            }
          // fall through
          case 85: break;
          case 22:
            { return FRAGMENT_PART;
            }
          // fall through
          case 86: break;
          case 23:
            { matchTimes++;
            }
          // fall through
          case 87: break;
          case 24:
            { yypushback(yylength()); yybegin(nextState);
            }
          // fall through
          case 88: break;
          case 25:
            { yypushback(yylength()); yybegin(IN_HEADER_FIELD_NAME);
            }
          // fall through
          case 89: break;
          case 26:
            { yybegin(IN_BODY); return WHITE_SPACE;
            }
          // fall through
          case 90: break;
          case 27:
            { return FIELD_NAME;
            }
          // fall through
          case 91: break;
          case 28:
            { yybegin(IN_HEADER_FIELD_VALUE); return COLON;
            }
          // fall through
          case 92: break;
          case 29:
            { yypushback(yylength()); yybegin(IN_HEADER_FIELD_VALUE_NO_SPACE);
            }
          // fall through
          case 93: break;
          case 30:
            { yypushback(yylength()); yybegin(IN_HEADER_FIELD_VALUE); return FIELD_VALUE;
            }
          // fall through
          case 94: break;
          case 31:
            { return INPUT_FILE_PATH_PART;
            }
          // fall through
          case 95: break;
          case 32:
            { yypushback(yylength()); yybegin(YYINITIAL);
            }
          // fall through
          case 96: break;
          case 33:
            { return OUTPUT_FILE_PATH_PART;
            }
          // fall through
          case 97: break;
          case 34:
            { yybegin(IN_DINAMIC_VARIABLE); return DOLLAR;
            }
          // fall through
          case 98: break;
          case 35:
            { return IDENTIFIER;
            }
          // fall through
          case 99: break;
          case 36:
            { if(nameFlag) return GLOBAL_NAME; else return GLOBAL_VALUE;
            }
          // fall through
          case 100: break;
          case 37:
            { yypushback(yylength()); yybegin(IN_GLOBAL_SCRIPT_END); return SCRIPT_BODY_PAET;
            }
          // fall through
          case 101: break;
          case 38:
            { yypushback(yylength()); yybegin(IN_PRE_SCRIPT_END); return SCRIPT_BODY_PAET;
            }
          // fall through
          case 102: break;
          case 39:
            { return SCHEMA_PART;
            }
          // fall through
          case 103: break;
          case 40:
            { nextState = IN_HOST; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE;
            }
          // fall through
          case 104: break;
          case 41:
            { nextState = IN_PATH; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE;
            }
          // fall through
          case 105: break;
          case 42:
            { nextState = IN_QUERY; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE;
            }
          // fall through
          case 106: break;
          case 43:
            { yybegin(IN_INPUT_FILE_PATH); return INPUT_FILE_SIGN;
            }
          // fall through
          case 107: break;
          case 44:
            { nextState = IN_HEADER_FIELD_VALUE; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE;
            }
          // fall through
          case 108: break;
          case 45:
            { yypushback(yylength()); yybegin(IN_POST_SCRIPT_END); return SCRIPT_BODY_PAET;
            }
          // fall through
          case 109: break;
          case 46:
            { yybegin(IN_OUTPUT_FILE); return END_SCRIPT_BRACE;
            }
          // fall through
          case 110: break;
          case 47:
            { yybegin(nextState); return END_VARIABLE_BRACE;
            }
          // fall through
          case 111: break;
          case 48:
            { nextState = IN_GLOBAL_VARIABLE; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE;
            }
          // fall through
          case 112: break;
          case 49:
            { nameFlag = true; yybegin(IN_DIRECTION_COMMENT); return DIRECTION_COMMENT_START;
            }
          // fall through
          case 113: break;
          case 50:
            { yypushback(1); return LINE_COMMENT;
            }
          // fall through
          case 114: break;
          case 51:
            { yybegin(YYINITIAL); return END_SCRIPT_BRACE;
            }
          // fall through
          case 115: break;
          case 52:
            { yybegin(IN_HOST); return SCHEMA_SEPARATE;
            }
          // fall through
          case 116: break;
          case 53:
            { nextState = IN_MULTIPART; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this);
            }
          // fall through
          case 117: break;
          case 54:
            { nextState = IN_OUTPUT_FILE_PATH; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this);
            }
          // fall through
          case 118: break;
          case 55:
            { yybegin(IN_OUTPUT_FILE_PATH); return OUTPUT_FILE_SIGN;
            }
          // fall through
          case 119: break;
          case 56:
            { return OUTPUT_FILE_SIGN;
            }
          // fall through
          case 120: break;
          case 57:
            { yybegin(detectBoundaryState(yytext())); return MESSAGE_BOUNDARY;
            }
          // fall through
          case 121: break;
          case 58:
            { return REQUEST_COMMENT;
            }
          // fall through
          case 122: break;
          case 59:
            { nextState = YYINITIAL; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this);
            }
          // fall through
          case 123: break;
          case 60:
            { yybegin(IN_PRE_SCRIPT); return IN_START_SCRIPT_BRACE;
            }
          // fall through
          case 124: break;
          case 61:
            { nextState = IN_POST_SCRIPT; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this);
            }
          // fall through
          case 125: break;
          case 62:
            { return OUT_START_SCRIPT_BRACE;
            }
          // fall through
          case 126: break;
          case 63:
            { yybegin(IN_GLOBAL_SCRIPT); return GLOBAL_START_SCRIPT_BRACE;
            }
          // fall through
          case 127: break;
          case 64:
            { return HTTP_VERSION;
            }
          // fall through
          case 128: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
