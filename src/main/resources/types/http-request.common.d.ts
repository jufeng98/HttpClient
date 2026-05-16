// noinspection JSUnusedGlobalSymbols

/**
 * 持有请求会话的元数据信息,例如全局变量
 */
declare const client: HttpRequest;

/**
 * 该对象持有有关当前请求的信息
 */
declare const request: HttpClientRequest;

// @ts-ignore
declare const console: Console;

/**
 * HttpRequest的会话在IDE启动时启动，在IDE关闭时结束，IDE重新启动后不会保留值。
 */
interface HttpRequest {
    /**
     * 在后置js处理器中定义的全局变量可以用作HTTP请求中的变量
     *
     * 例子:
     * <pre>
     * ### 登录请求接收token作为json主体的属性
     * GET https://example.com/login
     * > {%
     *    client.global.set("token", response.body.token)
     * %}
     * </pre>
     *
     * <pre>
     * ### 使用收到的token执行请求
     * GET http://example.com/get
     * Authorization: Bearer {{token}}
     * </pre>
     */
    global: Variables;

    /**
     * 打印“args”数组，然后终止该行。如果“args”的元素不是“string”，则函数将其转换为字符串。
     * 此外，它还将JS对象和数组作为其“JSON.stringify”表示形式打印出来。
     */
    log(...args: any[]): void;
}

/**
 * 存储变量，可用于定义、取消定义或检索变量。
 */
interface Variables {
    /**
     * 保存名为“varName”的变量，并将其值设置为“varValue”。
     */
    set(varName: string, varValue: any): void;

    /**
     * 返回变量“varName”的值。
     */
    get(varName: string): any;

    /**
     * 检查是否未定义变量。
     */
    isEmpty(): boolean;

    /**
     * 清除变量 'varName'.
     * @param varName {string}
     */
    clear(varName: string): void;

    /**
     * 清除所有变量.
     */
    clearAll(): void;
}

/**
 * 关于请求的信息，包括变量、URL等等
 */
interface HttpClientRequest {
    /**
     * 用于发送此请求的环境
     */
    environment: RequestEnvironment

    /**
     * 当前请求变量，可以在前置js处理器中更新。这些变量不会在请求之间共享。
     */
    variables: RequestVariables

    /**
     * 全局变量
     */
    globalVariables: RequestVariables,

    /**
     * 当前请求的请求头
     */
    headers: RequestHeaders

    /**
     * 当前请求的方法类型
     */
    method: string

    iteration(): number

    templateValue(expressionNumber: number): string | boolean | number
}

/**
 * 用于访问当前请求头的对象。
 */
interface RequestHeaders {
    /**
     * 包含所有请求头的数组
     */
    all(): [RequestHeader]

    /**
     * 按名称搜索第一个请求头，如果没有这样的请求头，则返回null。
     * @param name 请求头名称
     */
    findByName(name: string): RequestHeader | null
}

/**
 * 用于发送请求的环境。包含http-client.env.json和http-client.private.env.json文件中的环境变量。
 */
interface RequestEnvironment {
    /**
     * 按变量名称检索变量值。如果没有这样的变量，则返回null。
     * @param name 变量名称
     */
    get(name: string): string | null
}


/**
 * 用于构造当前请求的变量。可以在前置js处理器中更新。
 */
interface RequestVariables {
    /**
     * 按名称检索请求变量值。如果没有这样的变量，则返回null
     * @param name 请求变量名称
     */
    get(name: string): object | null
}

/**
 * 关于请求头的信息
 */
interface RequestHeader {
    /**
     * 请求头名称
     */
    name: string;
    /**
     * 请求头值列表
     */
    values: [string]
}

/**
 * 使用JSONPath表达式从JSON对象中检索值。
 *
 * @param {any} obj - 要搜索的JSON对象。
 * @param {string} expression - 用于搜索的JSONPath表达式。
 * @return {any} - 使用JSONPath表达式在JSON对象中找到的值。
 */
declare function jsonPath(obj: any, expression: string): any;

/**
 * 使用XPath表达式从XML对象检索值。
 *
 * @param {any} obj - 使用XPath表达式搜索的obj:
 *                    - 如果obj是Node，则Element或Document返回带表达式的搜索结果。
 *                    - 在所有其他情况下为null。
 * @param {string} expression - 用于搜索的XPath表达式。
 * @return {any} - 使用XPath表达式在XML对象中找到的值。
 */
declare function xpath(obj: any, expression: string): any;

/**
 * 用于处理URL查询参数的实用类。
 */
// @ts-ignore
declare class URLSearchParams implements Iterable<[string, string]> {
    constructor(options?: string | object | [any])

    size: number

    append(name: string, value: string): void

    delete(name: string, value?: string): void

    get(name: string): string | null

    getAll(name: string): string[]

    has(name: string, value?: string): boolean

    set(name: string, value: string): void

    sort(): void

    toString(): string

    entries(): [[string, string]]

    forEach(callback: (key: string, value: string, searchParams?: string) => void): void

    keys(): [string]

    values(): [string]
}

interface Console {
    /**
     * 与“HttpRequest”接口中的“log”相同
     */
    log(...args: any[]): void
}

/**
 * Stub from es2015
 */
interface String {
    /**
     * Returns a nonnegative integer Number less than 1114112 (0x110000) that is the code point
     * value of the UTF-16 encoded code point starting at the string element at position pos in
     * the String resulting from converting this object to a String.
     * If there is no element at that position, the result is undefined.
     * If a valid UTF-16 surrogate pair does not begin at pos, the result is the code unit at pos.
     */
    codePointAt(pos: number): number | undefined;

    /**
     * Returns true if searchString appears as a substring of the result of converting this
     * object to a String, at one or more positions that are
     * greater than or equal to position; otherwise, returns false.
     * @param searchString search string
     * @param position If position is undefined, 0 is assumed, so as to search all of the String.
     */
    includes(searchString: string, position?: number): boolean;

    /**
     * Returns true if the sequence of elements of searchString converted to a String is the
     * same as the corresponding elements of this object (converted to a String) starting at
     * endPosition – length(this). Otherwise returns false.
     */
    endsWith(searchString: string, endPosition?: number): boolean;

    /**
     * Returns the String value result of normalizing the string into the normalization form
     * named by form as specified in Unicode Standard Annex #15, Unicode Normalization Forms.
     * @param form Applicable values: "NFC", "NFD", "NFKC", or "NFKD", If not specified default
     * is "NFC"
     */
    normalize(form: "NFC" | "NFD" | "NFKC" | "NFKD"): string;

    /**
     * Returns the String value result of normalizing the string into the normalization form
     * named by form as specified in Unicode Standard Annex #15, Unicode Normalization Forms.
     * @param form Applicable values: "NFC", "NFD", "NFKC", or "NFKD", If not specified default
     * is "NFC"
     */
    normalize(form?: string): string;

    /**
     * Returns a String value that is made from count copies appended together. If count is 0,
     * the empty string is returned.
     * @param count number of copies to append
     */
    repeat(count: number): string;

    /**
     * Returns true if the sequence of elements of searchString converted to a String is the
     * same as the corresponding elements of this object (converted to a String) starting at
     * position. Otherwise returns false.
     */
    startsWith(searchString: string, position?: number): boolean;

    /**
     * Returns an `<a>` HTML anchor element and sets the name attribute to the text value
     * @deprecated A legacy feature for browser compatibility
     * @param name
     */
    anchor(name: string): string;

    /**
     * Returns a `<big>` HTML element
     * @deprecated A legacy feature for browser compatibility
     */
    big(): string;

    /**
     * Returns a `<blink>` HTML element
     * @deprecated A legacy feature for browser compatibility
     */
    blink(): string;

    /**
     * Returns a `<b>` HTML element
     * @deprecated A legacy feature for browser compatibility
     */
    bold(): string;

    /**
     * Returns a `<tt>` HTML element
     * @deprecated A legacy feature for browser compatibility
     */
    fixed(): string;

    /**
     * Returns a `<font>` HTML element and sets the color attribute value
     * @deprecated A legacy feature for browser compatibility
     */
    fontcolor(color: string): string;

    /**
     * Returns a `<font>` HTML element and sets the size attribute value
     * @deprecated A legacy feature for browser compatibility
     */
    fontsize(size: number): string;

    /**
     * Returns a `<font>` HTML element and sets the size attribute value
     * @deprecated A legacy feature for browser compatibility
     */
    fontsize(size: string): string;

    /**
     * Returns an `<i>` HTML element
     * @deprecated A legacy feature for browser compatibility
     */
    italics(): string;

    /**
     * Returns an `<a>` HTML element and sets the href attribute value
     * @deprecated A legacy feature for browser compatibility
     */
    link(url: string): string;

    /**
     * Returns a `<small>` HTML element
     * @deprecated A legacy feature for browser compatibility
     */
    small(): string;

    /**
     * Returns a `<strike>` HTML element
     * @deprecated A legacy feature for browser compatibility
     */
    strike(): string;

    /**
     * Returns a `<sub>` HTML element
     * @deprecated A legacy feature for browser compatibility
     */
    sub(): string;

    /**
     * Returns a `<sup>` HTML element
     * @deprecated A legacy feature for browser compatibility
     */
    sup(): string;
}

/**
 * WEB-API 接口.
 */

// @ts-ignore
declare class Window {
    static atob(str: string): string;

    static btoa(bytes: string): string;
}

/**
 * 提供将XML或HTML源代码从字符串解析为DOM文档的能力。
 *
 * [MDN Reference](https://developer.mozilla.org/docs/Web/API/DOMParser)
 */
interface DOMParser {
    /**
     * 根据类型使用HTML或XML解析器解析字符串，并返回结果文档。类型可以是“text/html”（将调用html解析器），或“text/xml”、“application/xml”，“application/xhtml+xml”或“image/svg+xml”中的任何一个（将调用xml解析器）。
     *
     * 对于XML解析器，如果无法解析字符串，则返回的Document将包含描述结果错误的元素。
     *
     * 请注意，在解析过程中不会计算脚本元素，并且生成的文档的编码将始终是UTF-8。
     *
     * 除上述类型之外的值将导致抛出TypeError异常。
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/DOMParser/parseFromString)
     */
    parseFromString(string: string, type: DOMParserSupportedType): Document;
}

declare var DOMParser: {
    prototype: DOMParser;
    new(): DOMParser;
};

interface ChildNode extends Node {
}

declare var ChildNode: {
    prototype: ChildNode;
    new(): ChildNode;
};

/**
 * Node是一个接口，许多DOM API对象类型继承自该接口。它允许对这些类型进行类似处理；例如，继承相同的方法集，或以相同的方式进行测试。
 *
 * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node)
 */
interface Node {
    /**
     * 返回节点的节点文档的文档基URL。
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/baseURI)
     */
    readonly baseURI: string;
    /**
     * 返回子节点列表
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/childNodes)
     */
    readonly childNodes: NodeListOf<ChildNode>;
    /**
     * 返回第一个子节点
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/firstChild)
     */
    readonly firstChild: ChildNode | null;
    /**
     * 如果节点已连接，则返回true，否则返回false。
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/isConnected)
     */
    readonly isConnected: boolean;
    /**
     * 返回最后一个子节点
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/lastChild)
     */
    readonly lastChild: ChildNode | null;
    /**
     * 返回下一个兄弟节点
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/nextSibling)
     */
    readonly nextSibling: ChildNode | null;
    /**
     * 返回节点名称
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/nodeName)
     */
    readonly nodeName: string;
    /**
     * 返回节点类型
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/nodeType)
     */
    readonly nodeType: number;
    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/nodeValue) */
    nodeValue: string | null;
    /**
     * 返回节点文档。对于文档返回null。
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/ownerDocument)
     */
    readonly ownerDocument: Document | null;
    /**
     * 返回父节点
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/parentNode)
     */
    readonly parentNode: ParentNode | null;
    /**
     * 返回前一个兄弟节点
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/previousSibling)
     */
    readonly previousSibling: ChildNode | null;
    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/textContent) */
    textContent: string | null;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/appendChild) */
    appendChild<T extends Node>(node: T): T;

    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/cloneNode)
     */
    cloneNode(deep?: boolean): Node;

    /**
     * 返回一个位掩码，指示其他节点相对于节点的位置。
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/compareDocumentPosition)
     */
    compareDocumentPosition(other: Node): number;

    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/contains)
     */
    contains(other: Node | null): boolean;

    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/getRootNode)
     */
    getRootNode(options?: GetRootNodeOptions): Node;

    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/hasChildNodes)
     */
    hasChildNodes(): boolean;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/insertBefore) */
    insertBefore<T extends Node>(node: T, child: Node | null): T;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/isDefaultNamespace) */
    isDefaultNamespace(namespace: string | null): boolean;

    /**
     * 返回node和otherNode是否具有相同的属性。
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/isEqualNode)
     */
    isEqualNode(otherNode: Node | null): boolean;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/isSameNode) */
    isSameNode(otherNode: Node | null): boolean;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/lookupNamespaceURI) */
    lookupNamespaceURI(prefix: string | null): string | null;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/lookupPrefix) */
    lookupPrefix(namespace: string | null): string | null;

    /**
     * 删除空的独占文本节点，并将剩余的连续独占文本节点的数据连接到其第一个节点中。
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/normalize)
     */
    normalize(): void;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/removeChild) */
    removeChild<T extends Node>(child: T): T;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/Node/replaceChild) */
    replaceChild<T extends Node>(node: Node, child: T): T;

    /** node is an element. */
    readonly ELEMENT_NODE: 1;
    readonly ATTRIBUTE_NODE: 2;
    /** node is a Text node. */
    readonly TEXT_NODE: 3;
    /** node is a CDATASection node. */
    readonly CDATA_SECTION_NODE: 4;
    readonly ENTITY_REFERENCE_NODE: 5;
    readonly ENTITY_NODE: 6;
    /** node is a ProcessingInstruction node. */
    readonly PROCESSING_INSTRUCTION_NODE: 7;
    /** node is a Comment node. */
    readonly COMMENT_NODE: 8;
    /** node is a document. */
    readonly DOCUMENT_NODE: 9;
    /** node is a doctype. */
    readonly DOCUMENT_TYPE_NODE: 10;
    /** node is a DocumentFragment node. */
    readonly DOCUMENT_FRAGMENT_NODE: 11;
    readonly NOTATION_NODE: 12;
    /** Set when node and other are not in the same tree. */
    readonly DOCUMENT_POSITION_DISCONNECTED: 0x01;
    /** Set when other is preceding node. */
    readonly DOCUMENT_POSITION_PRECEDING: 0x02;
    /** Set when other is following node. */
    readonly DOCUMENT_POSITION_FOLLOWING: 0x04;
    /** Set when other is an ancestor of node. */
    readonly DOCUMENT_POSITION_CONTAINS: 0x08;
    /** Set when other is a descendant of node. */
    readonly DOCUMENT_POSITION_CONTAINED_BY: 0x10;
    readonly DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: 0x20;
}

declare var Node: {
    prototype: Node;
    new(): Node;
    /** node is an element. */
    readonly ELEMENT_NODE: 1;
    readonly ATTRIBUTE_NODE: 2;
    /** node is a Text node. */
    readonly TEXT_NODE: 3;
    /** node is a CDATASection node. */
    readonly CDATA_SECTION_NODE: 4;
    readonly ENTITY_REFERENCE_NODE: 5;
    readonly ENTITY_NODE: 6;
    /** node is a ProcessingInstruction node. */
    readonly PROCESSING_INSTRUCTION_NODE: 7;
    /** node is a Comment node. */
    readonly COMMENT_NODE: 8;
    /** node is a document. */
    readonly DOCUMENT_NODE: 9;
    /** node is a doctype. */
    readonly DOCUMENT_TYPE_NODE: 10;
    /** node is a DocumentFragment node. */
    readonly DOCUMENT_FRAGMENT_NODE: 11;
    readonly NOTATION_NODE: 12;
    /** Set when node and other are not in the same tree. */
    readonly DOCUMENT_POSITION_DISCONNECTED: 0x01;
    /** Set when other is preceding node. */
    readonly DOCUMENT_POSITION_PRECEDING: 0x02;
    /** Set when other is following node. */
    readonly DOCUMENT_POSITION_FOLLOWING: 0x04;
    /** Set when other is an ancestor of node. */
    readonly DOCUMENT_POSITION_CONTAINS: 0x08;
    /** Set when other is a descendant of node. */
    readonly DOCUMENT_POSITION_CONTAINED_BY: 0x10;
    readonly DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: 0x20;
};

/**
 * CharacterData抽象接口表示包含字符的Node对象。这是一个抽象接口，意味着没有任何CharacterData类型的对象：它是由其他非抽象接口实现的，如Text、Comment或ProcessingInstruction。
 *
 * [MDN Reference](https://developer.mozilla.org/docs/Web/API/CharacterData)
 */
interface CharacterData extends Node, ChildNode {
    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/CharacterData/data) */
    data: string;
    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/CharacterData/length) */
    readonly length: number;
    readonly ownerDocument: Document;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/CharacterData/appendData) */
    appendData(data: string): void;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/CharacterData/deleteData) */
    deleteData(offset: number, count: number): void;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/CharacterData/insertData) */
    insertData(offset: number, data: string): void;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/CharacterData/replaceData) */
    replaceData(offset: number, count: number, data: string): void;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/CharacterData/substringData) */
    substringData(offset: number, count: number): string;
}

declare var CharacterData: {
    prototype: CharacterData;
    new(): CharacterData;
};

/**
 * Element或Attr的文本内容。如果一个元素的内容中没有标记，则它有一个子元素实现包含该元素文本的Text。但是，如果元素包含标记，它将被解析为信息项和形成其子项的文本节点。
 *
 * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Text)
 */
interface Text extends CharacterData {
    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Text/wholeText)
     */
    readonly wholeText: string;

    /**
     * 以给定的偏移量分割数据，并将剩余部分作为文本节点返回。
     *
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Text/splitText)
     */
    splitText(offset: number): Text;
}

// @ts-ignore
declare var Text: {
    prototype: Text;
    new(data: string): Text;
};

/**
 * 一个CDATA部分，可以在XML中使用，以包含未转义文本的扩展部分。符号<和&不需要像通常在CDATA部分中那样转义。
 *
 * [MDN Reference](https://developer.mozilla.org/docs/Web/API/CDATASection)
 */
interface CDATASection extends Text {
}

declare var CDATASection: {
    prototype: CDATASection;
    new(): CDATASection;
};

/**
 * 元素是最通用的基类，文档中的所有对象都从中继承。它只有各种元素共有的方法和属性。更具体的类继承自Element。
 *
 * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Element)
 */
interface Element extends Node {
    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Element/id)
     */
    id: string;
    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Element/tagName)
     */
    readonly tagName: string;
    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Element/className)
     */
    className: string;

    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Element/getAttribute)
     */
    getAttribute(qualifiedName: string): string | null;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/Element/getAttributeNode) */
    getAttributeNode(qualifiedName: string): Attr | null;

    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/Element/setAttributeNode) */
    setAttributeNode(attr: Attr): Attr | null;
}

declare var Element: {
    prototype: Element;
    new(): Element;
};

interface ElementCreationOptions {
    is?: string;
}

/**
 * 任何加载到浏览器中的网页，都可以作为进入网页内容的入口点，即DOM树。
 *
 * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Document)
 */
interface Document extends Element, Node {
    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Document/documentElement)
     */
    // @ts-ignore
    readonly documentElement: Element;

    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Document/createAttribute)
     * @param localName
     */
    createAttribute(localName: string): Attr;

    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Document/createCDATASection)
     */
    createCDATASection(data: string): CDATASection;

    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Document/createElement)
     * @param tagName
     * @param options
     */
    createElement(tagName: string, options?: ElementCreationOptions): Element;

    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Document/createTextNode)
     */
    createTextNode(data: string): Text;

    getElementById(elementId: string): Element | null;

    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Document/getElementsByClassName)
     */
    getElementsByClassName(classNames: string): NodeListOf<Element>;

    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Document/getElementsByName)
     */
    getElementsByName(elementName: string): NodeListOf<Element>;

    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/Document/getElementsByTagName)
     */
    getElementsByTagName(qualifiedName: string): NodeListOf<Element>;
}

declare var Document: {
    prototype: Document;
    new(): Document;
};

/**
 * 提供serializeToString（）方法来构造表示DOM树的XML字符串。
 *
 * [MDN Reference](https://developer.mozilla.org/docs/Web/API/XMLSerializer)
 */
interface XMLSerializer {
    /** [MDN Reference](https://developer.mozilla.org/docs/Web/API/XMLSerializer/serializeToString) */
    serializeToString(root: Node): string;
}

declare var XMLSerializer: {
    prototype: XMLSerializer;
    new(): XMLSerializer;
};

