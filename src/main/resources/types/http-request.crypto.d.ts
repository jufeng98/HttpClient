// noinspection JSUnusedGlobalSymbols

/**
 * 用于访问 HttpRequest Crypto API 的对象
 */
// @ts-ignore
declare const crypto: CryptoSupport;

/**
 * cryptography的一些有用的函数
 */
interface CryptoSupport {
    /**
     * SHA-1 digest builder
     */
    sha1(): DigestBuilder;

    /**
     * SHA-256 digest builder
     */
    sha256(): DigestBuilder;

    /**
     * SHA-384 digest builder
     */
    sha384(): DigestBuilder;

    /**
     * SHA-512 digest builder
     */
    sha512(): DigestBuilder;

    /**
     * MD-5 digest builder
     */
    md5(): DigestBuilder;

    /**
     * API for hmac
     */
    hmac: HmacSupport;
}

/**
 * 消化池构建器。“update*”方法的顺序调用将字节附加到结果消息中。
 */
interface DigestBuilder {
    /**
     * 附加以文本形式呈现的数据
     * @param textInput 用于附加到消息的数据
     * @param encoding 用于将文本解码为字节的编码。默认情况下，UTF-8
     */
    updateWithText(textInput: string, encoding?: string): DigestBuilder;

    /**
     * 附加以16进制十六进制文本表示的数据
     * @param hexInput 用于附加到消息的数据
     */
    updateWithHex(hexInput: string): DigestBuilder;

    /**
     * 附加以Base64编码文本形式呈现的数据
     * @param base64Input 用于附加到消息的数据
     * @param urlSafe “base64Input”编码为urlSafe的Base64变体。默认情况下，false
     */
    updateWithBase64(base64Input: string, urlSafe?: boolean): DigestBuilder;

    /**
     * 从包含的消息构造摘要。
     */
    digest(): Digest;
}

/**
 * 包含摘要字节的对象
 */
interface Digest {
    /**
     * 返回编码为16进制的字符串
     */
    toHex(): string;

    /**
     * 返回编码为Base64字符串的字节
     * @param urlSafe 如果为true，将使用Base64的url安全变体。默认情况下，false
     */
    toBase64(urlSafe?: boolean): string;
}

/**
 * 用于 HMAC 的 API
 */
interface HmacSupport {
    /**
     * SHA-1 HMAC builder
     */
    sha1(): HmacInitializer;

    /**
     * SHA-256 HMAC builder
     */
    sha256(): HmacInitializer;

    /**
     * SHA-384 HMAC builder
     */
    sha384(): HmacInitializer;

    /**
     * SHA-512 HMAC builder
     */
    sha512(): HmacInitializer;

    /**
     * MD-5 HMAC builder
     */
    md5(): HmacInitializer;
}

/**
 * 用于使用私钥（secret）初始化HMAC的对象。
 */
interface HmacInitializer {
    /**
     * 使用文本形式的密钥初始化HMAC。使用编码转换为字节
     * @param textSecret HMAC secret
     * @param encoding By default, UTF-8
     */
    withTextSecret(textSecret: string, encoding?: string): DigestBuilder;

    /**
     * 使用16进制十六进制字符串形式的密钥初始化HMAC。
     * @param hexSecret HMAC secret
     */
    withHexSecret(hexSecret: string): DigestBuilder;

    /**
     * 使用Base64字符串形式的密钥初始化HMAC。
     * @param base64Secret HMAC secret
     * @param urlSafe By default, false
     */
    withBase64Secret(base64Secret: string, urlSafe?: string): DigestBuilder;
}
