// noinspection JSUnusedGlobalSymbols

/**
 * 该对象保存有关HTTP响应的信息。
 */
declare const response: HttpResponse;

interface HttpRequest {
    test(testName: string, func: Function): void;

    assert(condition: boolean, message?: string): void;

    exit(): void;
}

/**
 * 将响应表示为文本流。
 */
interface TextStreamResponse {
    /**
     * 将整个流表示为一个文本，并在该文本的每一行上订阅。
     * @param subscriber 在流的每一行上调用的函数
     * @param onFinish function to be called after the end of the stream
     */
    onEachLine(subscriber: (line: string | Document | object, unsubscribe: () => void) => void, onFinish?: () => void): void;

    /**
     * 为用户订阅服务器发送的每条消息。此方法应与定义良好的流协议一起使用，如WebSocket或GRPC。
     * @param subscriber 在流的每条消息上调用的函数。如果协议支持双向，则可以通过“输出”将答案发送到服务器
     * @param onFinish 在流结束后调用的函数
     */
    onEachMessage(
        subscriber: (
            message: string | Document | object,
            unsubscribe: () => void,
            output?: (answer: string) => void
        ) => void,
        onFinish?: () => void
    ): void
}

/**
 * HTTP响应数据对象，包含有关响应内容、响应头、状态等的信息。
 */
interface HttpResponse {
    /**
     * 响应内容，如果响应内容类型为JSON，则为字符串或JSON对象。
     */
    body: string | TextStreamResponse | Document | object;

    /**
     * 响应头
     */
    headers: ResponseHeaders;

    cookies: [Cookie];

    cookiesByName(name: string): [Cookie];

    /**
     * 响应状态, 例如 200, 404, 等等.
     */
    status: number;

    /**
     * 响应的'Content-Type'.
     */
    contentType: ContentType;
}

/**
 * 响应头存储，可用于检索有关标头值的数据。
 */
interface ResponseHeaders {
    /**
     * 检索'headerName'响应标头的第一个值，否则为null。
     */
    valueOf(headerName: string): string | null;

    /**
     * 检索'headerName'响应标头的所有值。如果带有'headerName'的标头不存在，则返回空列表。
     */
    valuesOf(headerName: string): string[];
}

/**
 * 内容类型数据对象，包含来自“Content-type”响应标头的信息。
 */
interface ContentType {
    /**
     * 响应的MIME type,
     * 例如 'text/plain', 'text/xml', 'application/json'.
     */
    mimeType: string;

    /**
     * 响应字符集的字符串表示, 例如 utf-8.
     */
    charset: string;
}

interface HttpClientRequest {
    /**
     * 当前请求的url
     */
    url(): string;

    /**
     * 当前请求的body文本
     */
    body(): string;
}

interface RequestHeader {
    /**
     * 请求头的值
     */
    value(): string
}

interface Cookie {
    domain: string;
    path: string;
    name: string;
    value: string;
    expiresAt: number;
}