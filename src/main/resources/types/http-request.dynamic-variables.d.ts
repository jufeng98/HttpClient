// noinspection JSUnusedGlobalSymbols

declare const $env: EnvVariables
declare const $property: EnvVariables

interface EnvVariables {
    [key: string]: string;
}

declare const $historyFolder: string;

/**
 * <div class="content">This dynamic variable generates the current timestamp in ISO 8601 format for UTC timezone</div><table class="sections"><tr><td class="section" valign="top"><p>Sample value:</p></td><td valign="top"><b>2025-08-23T14:35:32.495636Z</b></td></tr></table>
 */
declare const $isoTimestamp: string;

declare const $projectRoot: string;

declare const $mvnTarget: string;

/**
 * <a href="https://javadoc.io/doc/com.github.javafaker/javafaker/latest/com/github/javafaker/package-summary.html">Faker API</a>
 */
declare const $random: RandomVariables

interface FakerCommon {
    [key: string]: string;
}

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
    beer: FakerBeer;
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
    lorem: FakerLorem
    name: FakerName
    app: FakerCommon
    options: FakerOptions
    phoneNumber: FakerPhoneNumber
    number: FakerCommon
    shakespeare: FakerShakespeare
    superhero: FakerSuperhero
    nation: FakerCommon
    university: FakerUniversity
    internet: FakerInternet
    animal: FakerCommon
    team: FakerTeam
    programmingLanguage: FakerTeam
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

/**
 * 示例值: 2023-08-23 14:35:32
 */
declare const $datetime: string;

/**
 * 生成 Unix 时间戳, 用法: $timestampFull(0, 15, 30), 第一个参数 0 表示今天, 1表示明天以此类推, 第二个参数 15 表示 15点, 第三个参数 30 表示 30分, 示例值: 1644214400000
 */
declare function $timestampFull(day: number, hour: number, sec: number): string;

/**
 * 生成 Unix 日期时间戳, 用法: $timestampDate(0), 0 表示今天, 1 表示明天以此类推, 示例值: 1644214400000
 */
declare function $timestampDate(day: number): string;

/**
 * 生成格式化日期, 用法: $date(0, ''yyyy-MM-dd''), 第一个参数 0 表示今天, 1 表示明天以此类推; 第二个参数可选(默认yyyy-MM-dd). 示例值: 2025-02-02
 */
declare function $date(day: number, pattern: string | undefined): string;

/**
 * 引入 js 文件
 */
declare function require(path: string): string;

/**
 * 按 UTF-8 格式读取文件文本内容并转换为 base64 返回
 */
declare function $fileToBase64(path: string): string;

/**
 * 同 $fileToBase64 方法
 */
declare function $imageToBase64(path: string): string;

/**
 * 按 UTF-8 格式读取文件文本内容并返回
 */
declare function $readString(path: string): string;

/**
 * 转换 base64 后保存到 path文件
 */
declare function $base64ToFile(base64: string, path: string): void;

/**
 * 休眠 lng ms
 */
declare function sleep(lng: number): void;
