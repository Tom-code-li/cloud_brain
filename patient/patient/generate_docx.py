"""
将 模块材料包-患者系统.md 转换为 Word 文档 (.docx)
纯标准库实现，无需安装 python-docx

使用方法：
  python generate_docx.py
"""

import re
import os
import zipfile
import xml.etree.ElementTree as ET
from xml.sax.saxutils import escape as xml_escape
from datetime import datetime


# ========== OOXML 模板 ==========

CONTENT_TYPES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Default Extension="png" ContentType="image/png"/>
  <Default Extension="jpg" ContentType="image/jpeg"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>"""

RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

WORD_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

STYLES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
          xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
          xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="微软雅黑" w:hAnsi="微软雅黑" w:eastAsia="微软雅黑"/>
        <w:sz w:val="22"/>
        <w:szCs w:val="22"/>
        <w:lang w:val="en-US" w:eastAsia="zh-CN"/>
      </w:rPr>
    </w:rPrDefault>
    <w:pPrDefault/>
  </w:docDefaults>
  <w:style w:type="paragraph" w:styleId="Normal" w:default="1">
    <w:name w:val="Normal"/>
    <w:pPr>
      <w:spacing w:line="360" w:lineRule="auto" w:after="120"/>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="微软雅黑" w:hAnsi="微软雅黑" w:eastAsia="微软雅黑"/>
      <w:sz w:val="22"/>
      <w:szCs w:val="22"/>
    </w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="heading1">
    <w:name w:val="heading 1"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="240" w:after="120"/>
    </w:pPr>
    <w:rPr>
      <w:b/>
      <w:sz w:val="36"/>
      <w:szCs w:val="36"/>
      <w:color w:val="1F4E79"/>
    </w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="heading2">
    <w:name w:val="heading 2"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="200" w:after="100"/>
    </w:pPr>
    <w:rPr>
      <w:b/>
      <w:sz w:val="30"/>
      <w:szCs w:val="30"/>
      <w:color w:val="2E75B6"/>
    </w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="heading3">
    <w:name w:val="heading 3"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="160" w:after="80"/>
    </w:pPr>
    <w:rPr>
      <w:b/>
      <w:sz w:val="26"/>
      <w:szCs w:val="26"/>
      <w:color w:val="2E75B6"/>
    </w:rPr>
  </w:style>
  <w:style w:type="character" w:styleId="bold">
    <w:name w:val="bold"/>
    <w:basedOn w:val="DefaultParagraphFont"/>
    <w:rPr>
      <w:b/>
    </w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="ListBullet">
    <w:name w:val="List Bullet"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:ind w:left="720" w:hanging="360"/>
      <w:spacing w:after="60"/>
    </w:pPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="ListNumber">
    <w:name w:val="List Number"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:ind w:left="720" w:hanging="360"/>
      <w:spacing w:after="60"/>
    </w:pPr>
  </w:style>
</w:styles>"""


class DocxBuilder:
    """使用标准库构建 .docx 文件"""

    def __init__(self):
        self.body_parts = []
        self.table_id = 0

    def _make_run(self, text, bold=False, italic=False, color=None, size=None, font=None):
        """生成 w:r 元素"""
        r = ET.Element("{http://schemas.openxmlformats.org/wordprocessingml/2006/main}r")
        rPr = ET.SubElement(r, "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}rPr")
        if bold:
            ET.SubElement(rPr, "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}b")
        if italic:
            ET.SubElement(rPr, "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}i")
        if color:
            c = ET.SubElement(rPr, "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}color")
            c.set("{http://schemas.openxmlformats.org/wordprocessingml/2006/main}val", color)
        if size:
            sz = ET.SubElement(rPr, "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}sz")
            sz.set("{http://schemas.openxmlformats.org/wordprocessingml/2006/main}val", str(size))
            szCs = ET.SubElement(rPr, "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}szCs")
            szCs.set("{http://schemas.openxmlformats.org/wordprocessingml/2006/main}val", str(size))
        if font:
            rFonts = ET.SubElement(rPr, "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}rFonts")
            rFonts.set("{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ascii", font)
            rFonts.set("{http://schemas.openxmlformats.org/wordprocessingml/2006/main}hAnsi", font)
            rFonts.set("{http://schemas.openxmlformats.org/wordprocessingml/2006/main}eastAsia", font)
        t = ET.SubElement(r, "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}t")
        t.set("{http://www.w3.org/XML/1998/namespace}space", "preserve")
        t.text = text
        return r

    def _make_paragraph(self, runs=None, style=None, alignment=None, spacing_after=None, spacing_before=None):
        """生成 w:p 元素"""
        ns = "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}"
        p = ET.Element(ns + "p")
        pPr = ET.SubElement(p, ns + "pPr")
        if style:
            pStyle = ET.SubElement(pPr, ns + "pStyle")
            pStyle.set(ns + "val", style)
        if alignment:
            jc = ET.SubElement(pPr, ns + "jc")
            jc.set(ns + "val", alignment)
        if spacing_after is not None:
            sp = ET.SubElement(pPr, ns + "spacing")
            sp.set(ns + "after", str(spacing_after))
        if spacing_before is not None:
            sp = pPr.find(ns + "spacing")
            if sp is None:
                sp = ET.SubElement(pPr, ns + "spacing")
            sp.set(ns + "before", str(spacing_before))
        if runs:
            for r in runs:
                p.append(r)
        return p

    def add_heading(self, text, level=1):
        style_map = {1: "heading1", 2: "heading2", 3: "heading3"}
        style = style_map.get(level, "heading1")
        runs = [self._make_run(text, bold=True)]
        p = self._make_paragraph(runs, style=style, spacing_before=240 if level == 1 else 160)
        self.body_parts.append(p)

    def add_paragraph(self, text, bold=False, alignment=None):
        """处理文本中的 **加粗** 标记"""
        ns = "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}"
        p = ET.Element(ns + "p")
        pPr = ET.SubElement(p, ns + "pPr")

        if alignment:
            jc = ET.SubElement(pPr, ns + "jc")
            jc.set(ns + "val", alignment)

        parts = re.split(r'(\*\*.*?\*\*)', text)
        has_content = False
        for part in parts:
            if not part:
                continue
            if part.startswith("**") and part.endswith("**"):
                r = self._make_run(part[2:-2], bold=True)
                p.append(r)
                has_content = True
            else:
                r = self._make_run(part)
                p.append(r)
                has_content = True

        if not has_content:
            r = self._make_run("")
            p.append(r)

        self.body_parts.append(p)

    def add_bullet(self, text):
        """添加无序列表项"""
        p = self._make_paragraph([self._make_run(text)], style="ListBullet", spacing_after=60)
        self.body_parts.append(p)

    def add_numbered(self, text):
        """添加有序列表项"""
        p = self._make_paragraph([self._make_run(text)], style="ListNumber", spacing_after=60)
        self.body_parts.append(p)

    def add_table(self, headers, rows):
        """添加表格"""
        ns = "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}"
        tbl = ET.Element(ns + "tbl")

        # table properties
        tblPr = ET.SubElement(tbl, ns + "tblPr")
        tblStyle = ET.SubElement(tblPr, ns + "tblStyle")
        tblStyle.set(ns + "val", "TableGrid")
        tblW = ET.SubElement(tblPr, ns + "tblW")
        tblW.set(ns + "w", "5000")
        tblW.set(ns + "type", "pct")
        tblBorders = ET.SubElement(tblPr, ns + "tblBorders")
        for border_name in ["top", "left", "bottom", "right", "insideH", "insideV"]:
            border = ET.SubElement(tblBorders, ns + border_name)
            border.set(ns + "val", "single")
            border.set(ns + "sz", "4")
            border.set(ns + "space", "0")
            border.set(ns + "color", "000000")

        col_count = max(len(headers), max(len(r) for r in rows) if rows else 0)

        # 列宽
        tblGrid = ET.SubElement(tbl, ns + "tblGrid")
        col_width = int(5000 / col_count) if col_count > 0 else 1000
        for _ in range(col_count):
            gridCol = ET.SubElement(tblGrid, ns + "gridCol")
            gridCol.set(ns + "w", str(col_width))

        def make_cell(text, is_header=False):
            cell = ET.Element(ns + "tc")
            tcPr = ET.SubElement(cell, ns + "tcPr")
            tcW = ET.SubElement(tcPr, ns + "tcW")
            tcW.set(ns + "w", str(col_width))
            tcW.set(ns + "type", "dxa")

            if is_header:
                shd = ET.SubElement(tcPr, ns + "shd")
                shd.set(ns + "val", "clear")
                shd.set(ns + "color", "auto")
                shd.set(ns + "fill", "1F4E79")

            p = ET.SubElement(cell, ns + "p")
            pPr = ET.SubElement(p, ns + "pPr")
            jc = ET.SubElement(pPr, ns + "jc")
            jc.set(ns + "val", "center")

            r = self._make_run(text, bold=is_header, color="FFFFFF" if is_header else None, size=18)
            p.append(r)
            return cell

        # 表头行
        if headers:
            tr = ET.SubElement(tbl, ns + "tr")
            for h in headers:
                tr.append(make_cell(h, is_header=True))

        # 数据行
        for row_data in rows:
            tr = ET.SubElement(tbl, ns + "tr")
            for i in range(col_count):
                cell_text = row_data[i] if i < len(row_data) else ""
                tr.append(make_cell(cell_text))

        self.body_parts.append(tbl)

        # 表后空行
        self.body_parts.append(self._make_paragraph([self._make_run("")], spacing_after=60))

    def add_code_block(self, text):
        """添加代码块（等宽字体灰色背景）"""
        ns = "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}"
        p = ET.Element(ns + "p")
        pPr = ET.SubElement(p, ns + "pPr")
        shd = ET.SubElement(pPr, ns + "shd")
        shd.set(ns + "val", "clear")
        shd.set(ns + "color", "auto")
        shd.set(ns + "fill", "F2F2F2")
        r = self._make_run(text, color="333333", size=18, font="Courier New")
        p.append(r)
        self.body_parts.append(p)

    def add_empty_line(self):
        p = self._make_paragraph([self._make_run("")])
        self.body_parts.append(p)

    def build(self):
        """构建完整的 document.xml"""
        ns = "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}"
        document = ET.Element(ns + "document")
        document.set("{http://schemas.openxmlformats.org/officeDocument/2006/relationships}xmlns:r",
                     "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
        body = ET.SubElement(document, ns + "body")

        for part in self.body_parts:
            body.append(part)

        # 节属性（页边距）
        sectPr = ET.SubElement(body, ns + "sectPr")
        pgSz = ET.SubElement(sectPr, ns + "pgSz")
        pgSz.set(ns + "w", "11906")  # A4
        pgSz.set(ns + "h", "16838")
        pgMar = ET.SubElement(sectPr, ns + "pgMar")
        pgMar.set(ns + "top", "1440")
        pgMar.set(ns + "right", "1800")
        pgMar.set(ns + "bottom", "1440")
        pgMar.set(ns + "left", "1800")
        pgMar.set(ns + "header", "720")
        pgMar.set(ns + "footer", "720")

        return ET.tostring(document, encoding="UTF-8", xml_declaration=True)


def parse_markdown_simple(md_path):
    """解析 Markdown 文件，逐行处理"""
    with open(md_path, "r", encoding="utf-8") as f:
        lines = f.readlines()

    docx = DocxBuilder()
    i = 0
    in_code_block = False
    code_lines = []
    in_table = False
    table_headers = []
    table_rows = []
    in_bullet_section = False

    while i < len(lines):
        line = lines[i].rstrip()

        # 跳过 frontmatter
        if line.strip() == "---" and i < 3:
            while i < len(lines):
                i += 1
                if i < len(lines) and lines[i].strip() == "---":
                    i += 1
                    break
            continue

        # 代码块
        if line.strip().startswith("```"):
            if in_code_block:
                docx.add_code_block("\n".join(code_lines))
                code_lines = []
                in_code_block = False
            else:
                in_code_block = True
            i += 1
            continue

        if in_code_block:
            code_lines.append(line)
            i += 1
            continue

        # 空行
        if line.strip() == "":
            i += 1
            continue

        # 表格行
        if line.startswith("|") and line.endswith("|"):
            cells = [c.strip() for c in line.split("|")[1:-1]]
            # 跳过分隔行
            if re.match(r"^[\s\-:]+$", "".join(cells)):
                i += 1
                continue
            if not in_table:
                in_table = True
                table_headers = cells
                table_rows = []
            else:
                table_rows.append(cells)
            i += 1
            continue
        else:
            if in_table:
                docx.add_table(table_headers, table_rows)
                in_table = False
                table_headers = []
                table_rows = []
                continue

        # 标题
        heading_match = re.match(r"^(#{1,3})\s+(.+)$", line)
        if heading_match:
            level = len(heading_match.group(1))
            text = heading_match.group(2).strip()
            # 去除编号如 "1.", "1）", "（1）"
            text = re.sub(r"^[（(]?\d+[）.)、]\s*", "", text)
            docx.add_heading(text, level=level)
            i += 1
            continue

        # 无序列表 * -
        list_match = re.match(r"^[\*\-\+]\s+(.+)$", line)
        if list_match:
            docx.add_bullet(list_match.group(1).strip())
            i += 1
            continue

        # 有序列表 1. 1） 1)
        olist_match = re.match(r"^\d+[\.\）\)]\s+(.+)$", line)
        if olist_match:
            docx.add_numbered(olist_match.group(1).strip())
            i += 1
            continue

        # 普通段落
        docx.add_paragraph(line)
        i += 1

    # 处理文件末尾未闭合的表格
    if in_table:
        docx.add_table(table_headers, table_rows)

    return docx.build()


def save_docx(docx_xml, output_path):
    """将 document.xml 打包为 .docx 文件"""
    with zipfile.ZipFile(output_path, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("[Content_Types].xml", CONTENT_TYPES_XML.encode("utf-8"))
        zf.writestr("_rels/.rels", RELS_XML.encode("utf-8"))
        zf.writestr("word/_rels/document.xml.rels", WORD_RELS_XML.encode("utf-8"))
        zf.writestr("word/styles.xml", STYLES_XML.encode("utf-8"))
        zf.writestr("word/document.xml", docx_xml)


def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    md_path = os.path.join(script_dir, "模块材料包-患者系统.md")
    docx_path = os.path.join(script_dir, "模块材料包-患者系统.docx")

    if not os.path.exists(md_path):
        print(f"✗ 找不到 Markdown 文件: {md_path}")
        return

    print("正在解析 Markdown...")
    docx_xml = parse_markdown_simple(md_path)

    print("正在生成 Word 文档...")
    save_docx(docx_xml, docx_path)

    print(f"✓ 生成成功: {docx_path}")


if __name__ == "__main__":
    main()
