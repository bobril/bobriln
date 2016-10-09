import * as b from './bobril';
export interface IViewData {
    style?: b.IBobrilStyles;
    children?: b.IBobrilChildren;
}
export declare function View(data?: IViewData, children?: b.IBobrilChildren): {
    tag: string;
    children: b.IBobrilChildren;
};
export interface ITextData {
    style?: b.IBobrilStyles;
    children?: b.IBobrilChildren;
}
export declare function Text(data?: IViewData, children?: b.IBobrilChildren): {
    tag: string;
    children: b.IBobrilChildren;
};
export interface IImageData {
    /** pass result of b.asset("pathToImage") */
    source: string | (string | number)[];
    style?: b.IBobrilStyles;
    children?: b.IBobrilChildren;
}
export declare function Image(data?: IViewData, children?: b.IBobrilChildren): {
    tag: string;
    children: b.IBobrilChildren;
};
export interface ISwitchData extends b.IValueData<boolean> {
    style?: b.IBobrilStyles;
}
export declare const Switch: b.IComponentFactory<ISwitchData>;
export interface ITextInputData {
    children?: b.IBobrilChildren;
    style?: b.IBobrilStyles;
    selectionStart?: number;
    selectionEnd?: number;
    onDetailChange?: (start: number, before: number, text: string) => void;
    onChange?: (value: string) => void;
    onSelectionChange?: (start: number, end: number) => void;
}
export declare const TextInput: b.IComponentFactory<ITextInputData>;
