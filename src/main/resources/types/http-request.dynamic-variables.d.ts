// noinspection JSUnusedGlobalSymbols

declare const $env: EnvVariables

interface EnvVariables {
    [key: string]: string;
}

declare const $historyFolder: string;

/**
 * <div class="content">This dynamic variable generates the current timestamp in ISO 8601 format for UTC timezone</div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>2025-08-23T14:35:32.495636Z</b></td></tr></table>
 */
declare const $isoTimestamp: string;

declare const $projectRoot: string;

/**
 * <a href="https://javadoc.io/doc/com.github.javafaker/javafaker/latest/com/github/javafaker/package-summary.html">Faker API</a>
 */
declare const $random: RandomVariables

interface FakerAddress {
    [key: string]: string;
}

interface FakerAncient {
    [key: string]: string;
}

interface FakerBeer {
    [key: string]: string;
}

interface FakerBool {
    [key: string]: string;
}

interface FakerBook {
    [key: string]: string;
}

interface FakerBusiness {
    [key: string]: string;
}

interface FakerChuckNorris {
    [key: string]: string;
}

interface FakerCode {
    [key: string]: string;
}

interface FakerColor {
    [key: string]: string;
}

interface FakerCommerce {
    [key: string]: string;
}

interface FakerCompany {
    [key: string]: string;
}

interface FakerCrypto {
    [key: string]: string;
}

interface FakerDateAndTime {
    [key: string]: string;
}

interface FakerEducator {
    [key: string]: string;
}

interface FakerFinance {
    [key: string]: string;
}

interface FakerHacker {
    [key: string]: string;
}

interface FakerIdNumber {
    [key: string]: string;
}

interface FakerInternet {
    [key: string]: string;
}

interface FakerLorem {
    [key: string]: string;
}

interface FakerName {
    [key: string]: string;
}

interface FakerNumber {
    [key: string]: string;
}

interface FakerOptions {
    [key: string]: string;
}

interface FakerPhoneNumber {
    [key: string]: string;
}

interface FakerShakespeare {
    [key: string]: string;
}

interface FakerSuperhero {
    [key: string]: string;
}

interface FakerTeam {
    [key: string]: string;
}

interface FakerUniversity {
    [key: string]: string;
}

interface RandomVariables {
    /**
     * <div class="content">This dynamic variable generates random sequence of uppercase and lowercase letters of length <code>length</code></div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>aBsseEasgZ</b></td></tr><tr><td class="section" valign="top"><p>Params:</p></td><td valign="top"><p><code>length</code> &ndash; Length of the generated string. Should be greater than 0.<br/><i>Required to be <b>INTEGER</b></i></p></td></tr></table>
     */
    // @ts-ignore
    alphabetic(length: number): string

    /**
     * <div class="content">This dynamic variable generates random sequence of letters, digits and <code>_</code> of length <code>length</code></div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>wEd1Ib5Jzi</b></td></tr><tr><td class="section" valign="top"><p>Params:</p></td><td valign="top"><p><code>length</code> &ndash; Length of the generated string. Should be greater than 0.<br/><i>Required to be <b>INTEGER</b></i></p></td></tr></table>
     */
    // @ts-ignore
    alphanumeric(length: number): string

    /**
     * <div class="content">This dynamic variable generates random email address</div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>asdaf@afad.com</b></td></tr></table>
     */
    email: string

    /**
     * <div class="content">This dynamic variable generates random float</div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>123.12</b></td></tr><tr><td class="section" valign="top"><p><b>Versions</b></p></td></tr><tr><td class="section" valign="top"><p>(from, to)</p></td><td valign="top"><p>This version generates float between <code>from</code> and <code>to</code></p><br/><p><code>from</code> &ndash; Lower bound for a random float (inclusive).<br/><i>Required to be <b>FLOAT</b></i></p><p><code>to</code> &ndash; Upper bound for a random float (exclusive).<br/><i>Required to be <b>FLOAT</b></i></p></td></tr><tr><td class="section" valign="top"><p>(to)</p></td><td valign="top"><p>This version generates float between 0 and <code>to</code></p><br/><p><code>to</code> &ndash; Upper bound for a random float (exclusive).<br/><i>Required to be <b>FLOAT</b></i></p></td></tr><tr><td class="section" valign="top"><p>()</p></td><td valign="top"><p>This version generates float between 0 and 1000 (inclusive)</p><br/></td></tr></table>
     */
    // @ts-ignore
    float(from: number, to: number): string

    /**
     * <div class="content">This dynamic variable generates random float</div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>123.12</b></td></tr><tr><td class="section" valign="top"><p><b>Versions</b></p></td></tr><tr><td class="section" valign="top"><p>(from, to)</p></td><td valign="top"><p>This version generates float between <code>from</code> and <code>to</code></p><br/><p><code>from</code> &ndash; Lower bound for a random float (inclusive).<br/><i>Required to be <b>FLOAT</b></i></p><p><code>to</code> &ndash; Upper bound for a random float (exclusive).<br/><i>Required to be <b>FLOAT</b></i></p></td></tr><tr><td class="section" valign="top"><p>(to)</p></td><td valign="top"><p>This version generates float between 0 and <code>to</code></p><br/><p><code>to</code> &ndash; Upper bound for a random float (exclusive).<br/><i>Required to be <b>FLOAT</b></i></p></td></tr><tr><td class="section" valign="top"><p>()</p></td><td valign="top"><p>This version generates float between 0 and 1000 (inclusive)</p><br/></td></tr></table>
     */
    float(to: number): string

    /**
     * <div class="content">This dynamic variable generates random float</div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>123.12</b></td></tr><tr><td class="section" valign="top"><p><b>Versions</b></p></td></tr><tr><td class="section" valign="top"><p>(from, to)</p></td><td valign="top"><p>This version generates float between <code>from</code> and <code>to</code></p><br/><p><code>from</code> &ndash; Lower bound for a random float (inclusive).<br/><i>Required to be <b>FLOAT</b></i></p><p><code>to</code> &ndash; Upper bound for a random float (exclusive).<br/><i>Required to be <b>FLOAT</b></i></p></td></tr><tr><td class="section" valign="top"><p>(to)</p></td><td valign="top"><p>This version generates float between 0 and <code>to</code></p><br/><p><code>to</code> &ndash; Upper bound for a random float (exclusive).<br/><i>Required to be <b>FLOAT</b></i></p></td></tr><tr><td class="section" valign="top"><p>()</p></td><td valign="top"><p>This version generates float between 0 and 1000 (inclusive)</p><br/></td></tr></table>
     */
    float(): string

    /**
     * <div class="content">This dynamic variable generates random hexadecimal string of length <code>length</code></div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>A012BCF</b></td></tr><tr><td class="section" valign="top"><p>Params:</p></td><td valign="top"><p><code>length</code> &ndash; Length of generated hexadecimal string. Should be greater than 0.<br/><i>Required to be <b>INTEGER</b></i></p></td></tr></table>
     */
    // @ts-ignore
    hexadecimal(length: number): string

    /**
     * <div class="content">This dynamic variable generates random integer</div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>123</b></td></tr><tr><td class="section" valign="top"><p><b>Versions</b></p></td></tr><tr><td class="section" valign="top"><p>(from, to)</p></td><td valign="top"><p>This version generates integer between <code>from</code> and <code>to</code></p><br/><p><code>from</code> &ndash; Lower bound for a random integer (inclusive).<br/><i>Required to be <b>INTEGER</b></i></p><p><code>to</code> &ndash; Upper bound for a random integer (exclusive).<br/><i>Required to be <b>INTEGER</b></i></p></td></tr><tr><td class="section" valign="top"><p>(to)</p></td><td valign="top"><p>This version generates integer between 0 and <code>to</code></p><br/><p><code>to</code> &ndash; Upper bound for a random integer (exclusive).<br/><i>Required to be <b>INTEGER</b></i></p></td></tr><tr><td class="section" valign="top"><p>()</p></td><td valign="top"><p>This version generates integer between 0 and 1000 (inclusive)</p><br/></td></tr></table>
     */
    // @ts-ignore
    integer(from: number, to: number): string

    /**
     * <div class="content">This dynamic variable generates random integer</div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>123</b></td></tr><tr><td class="section" valign="top"><p><b>Versions</b></p></td></tr><tr><td class="section" valign="top"><p>(from, to)</p></td><td valign="top"><p>This version generates integer between <code>from</code> and <code>to</code></p><br/><p><code>from</code> &ndash; Lower bound for a random integer (inclusive).<br/><i>Required to be <b>INTEGER</b></i></p><p><code>to</code> &ndash; Upper bound for a random integer (exclusive).<br/><i>Required to be <b>INTEGER</b></i></p></td></tr><tr><td class="section" valign="top"><p>(to)</p></td><td valign="top"><p>This version generates integer between 0 and <code>to</code></p><br/><p><code>to</code> &ndash; Upper bound for a random integer (exclusive).<br/><i>Required to be <b>INTEGER</b></i></p></td></tr><tr><td class="section" valign="top"><p>()</p></td><td valign="top"><p>This version generates integer between 0 and 1000 (inclusive)</p><br/></td></tr></table>
     */
    integer(to: number): string

    /**
     * <div class="content">This dynamic variable generates random integer</div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>123</b></td></tr><tr><td class="section" valign="top"><p><b>Versions</b></p></td></tr><tr><td class="section" valign="top"><p>(from, to)</p></td><td valign="top"><p>This version generates integer between <code>from</code> and <code>to</code></p><br/><p><code>from</code> &ndash; Lower bound for a random integer (inclusive).<br/><i>Required to be <b>INTEGER</b></i></p><p><code>to</code> &ndash; Upper bound for a random integer (exclusive).<br/><i>Required to be <b>INTEGER</b></i></p></td></tr><tr><td class="section" valign="top"><p>(to)</p></td><td valign="top"><p>This version generates integer between 0 and <code>to</code></p><br/><p><code>to</code> &ndash; Upper bound for a random integer (exclusive).<br/><i>Required to be <b>INTEGER</b></i></p></td></tr><tr><td class="section" valign="top"><p>()</p></td><td valign="top"><p>This version generates integer between 0 and 1000 (inclusive)</p><br/></td></tr></table>
     */
    integer(): string

    /**
     * <div class="content">This dynamic variable generates a new UUID-v4</div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>e9e87c05-82eb-4522-bc47-f0fcfdde4cab</b></td></tr></table>
     */
    uuid: string

    address: FakerAddress;
    ancient: FakerAncient;
    beer : FakerBeer;
    bool: FakerBool;
    book: FakerBook;
    business: FakerBusiness;
    chuckNorris: FakerChuckNorris;
    code: FakerCode;
    color: FakerColor;
    commerce: FakerCommerce;
    company: FakerCompany;
    crypto: FakerCrypto;
    dateAndTime: FakerDateAndTime
    educator: FakerEducator
    finance: FakerFinance
    hacker: FakerHacker
    idNumber: FakerIdNumber
    internet: FakerInternet
    lorem: FakerLorem
    name: FakerName
    number: FakerNumber
    options: FakerOptions
    phoneNumber: FakerPhoneNumber
    shakespeare: FakerShakespeare
    superhero: FakerSuperhero
    team: FakerTeam
    university: FakerUniversity
}
/**
 * <div class="content">This dynamic variable generates a random integer between 0 and 1000</div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>123</b></td></tr></table>
 */
declare const $randomInt: string;

/**
 * <div class="content">This dynamic variable generates the current Unix timestamp</div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>1563362218</b></td></tr></table>
 */
declare const $timestamp: string;

/**
 * <div class="content">This dynamic variable generates a new UUID-v4</div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>e9e87c05-82eb-4522-bc47-f0fcfdde4cab</b></td></tr></table>
 */
declare const $uuid: string;

