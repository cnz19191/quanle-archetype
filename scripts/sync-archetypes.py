#!/usr/bin/env python3
"""Build archetype-resources from the two benchmark projects."""

from __future__ import annotations

import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROPS = [
    "packageInPathFormat",
    "isiniuParentVersion",
    "errorCodePrefix",
    "rootArtifactId",
    "artifactId",
    "groupId",
    "version",
    "package",
    "author",
    "dollar",
]


def velocity_escape(text: str) -> str:
    tokens: dict[str, str] = {}
    for index, prop in enumerate(PROPS):
        key = f"__KEEP_PROP_{index}__"
        text = text.replace("${" + prop + "}", key)
        tokens[key] = "${" + prop + "}"
    text = text.replace("${", "${dollar}{")
    text = re.sub(r"#\{([^{}]*)\}", lambda match: "#[[#{" + match.group(1) + "}]]#", text)
    lines = []
    for line in text.splitlines(keepends=True):
        if line.startswith("#") and not line.startswith("#[["):
            line = re.sub(r"^(#+)", lambda match: "#[[" + match.group(1) + "]]#", line, count=1)
        lines.append(line)
    text = "".join(lines)
    for key, value in tokens.items():
        text = text.replace(key, value)
    return text


def clean_agents(text: str) -> str:
    return text.replace(
        "脚手架仓库中的标杆取值：根包 `com.isiniu.demo`（单模块标杆为 `com.isiniu.single`），作者 `isiniu`，错误码前缀 `ISN`，标杆领域 `auth`（鉴权）、`issue`（问题管理）、`comment`（评论）。",
        "本项目的实际取值见下方【项目配置】；标杆领域是 `auth`（鉴权）、`issue`（问题管理）、`comment`（评论）。",
    )


def rewrite_business(text: str, package: str) -> str:
    text = text.replace(package, "${package}")
    text = text.replace("@author isiniu", "@author ${author}")
    text = re.sub(r"ISN(\d{4})", r"${errorCodePrefix}\1", text)
    return text


def write(path: Path, text: str, escape: bool = True) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    if escape:
        text = velocity_escape(text)
    if not text.endswith("\n"):
        text += "\n"
    path.write_text(text, encoding="utf-8")


def reset(path: Path) -> None:
    if path.exists():
        shutil.rmtree(path)
    path.mkdir(parents=True)


def copy_docs(src: Path, dest: Path) -> None:
    if not src.exists():
        return
    for file in src.rglob("*"):
        if not file.is_file():
            continue
        rel = file.relative_to(src)
        text = rewrite_business(file.read_text(encoding="utf-8"), "com.isiniu.demo")
        text = text.replace("isiniu-demo", "${rootArtifactId}")
        text = text.replace("isiniu-single-demo", "${rootArtifactId}")
        write(dest / rel, text)


def modular_parent_pom() -> str:
    return """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.isiniu</groupId>
        <artifactId>isiniu-parent</artifactId>
        <version>${isiniuParentVersion}</version>
        <relativePath/>
    </parent>

    <groupId>${groupId}</groupId>
    <artifactId>${rootArtifactId}</artifactId>
    <version>${version}</version>
    <packaging>pom</packaging>
    <name>${rootArtifactId}</name>
    <description>模块化单体。领域之间只通过 api 模块协作。</description>

    <modules>
        <module>${rootArtifactId}-kernel</module>
        <module>${rootArtifactId}-web</module>
        <module>${rootArtifactId}-auth-api</module>
        <module>${rootArtifactId}-auth</module>
        <module>${rootArtifactId}-issue-api</module>
        <module>${rootArtifactId}-issue</module>
        <module>${rootArtifactId}-comment-api</module>
        <module>${rootArtifactId}-comment</module>
        <module>${rootArtifactId}-portal</module>
        <module>${rootArtifactId}-bootstrap</module>
    </modules>
</project>
"""


def transform_child_pom(text: str) -> str:
    text = text.replace(
        """    <parent>
        <groupId>com.isiniu</groupId>
        <artifactId>isiniu-demo</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>""",
        """    <parent>
        <groupId>${groupId}</groupId>
        <artifactId>${rootArtifactId}</artifactId>
        <version>${version}</version>
    </parent>""",
    )
    text = text.replace("isiniu-demo", "${rootArtifactId}")
    text = text.replace(
        "<groupId>com.isiniu</groupId>\n            <artifactId>${rootArtifactId}",
        "<groupId>${groupId}</groupId>\n            <artifactId>${rootArtifactId}",
    )
    text = text.replace("com.isiniu:${rootArtifactId}", "${groupId}:${rootArtifactId}")
    return text


def copy_module_sources(module_dir: Path, out_module: Path) -> None:
    for file in module_dir.rglob("*"):
        if not file.is_file() or "target" in file.parts:
            continue
        rel = file.relative_to(module_dir).as_posix()
        if rel == "pom.xml" or rel == "AGENTS.md":
            continue
        if not rel.startswith("src/") and rel != "Dockerfile":
            if not rel.startswith("docs/"):
                continue
        text = file.read_text(encoding="utf-8")
        text = rewrite_business(text, "com.isiniu.demo")
        text = text.replace("isiniu_demo", "app")
        text = text.replace("isiniu-demo", "${rootArtifactId}")
        path = rel.replace("com/isiniu/demo", "__packageInPathFormat__")
        write(out_module / path, text)


def build_modular(out: Path) -> None:
    reset(out)
    demo = ROOT / "isiniu-demo"
    write(out / "pom.xml", modular_parent_pom())
    write(out / "README.md", (ROOT / "archetype-src" / "modular-README.md").read_text(encoding="utf-8"))
    write(out / "CLAUDE.md", "@AGENTS.md\n", escape=False)
    write(out / "AGENTS.md", clean_agents((ROOT / "AGENTS.md").read_text(encoding="utf-8")))
    write(out / ".gitignore", (ROOT / "archetype-src" / "gitignore.txt").read_text(encoding="utf-8"))
    write(out / ".github" / "workflows" / "ci.yml", (ROOT / "archetype-src" / "ci.yml").read_text(encoding="utf-8"))
    copy_docs(ROOT / "docs", out / "docs")
    copy_docs(demo / "docs", out / "docs")
    for module in sorted(path for path in demo.iterdir() if path.is_dir() and path.name.startswith("isiniu-demo-")):
        name = module.name.replace("isiniu-demo", "__rootArtifactId__")
        dest = out / name
        copy_module_sources(module, dest)
        write(dest / "pom.xml", transform_child_pom((module / "pom.xml").read_text(encoding="utf-8")))
        agents = module / "AGENTS.md"
        if agents.exists():
            text = rewrite_business(agents.read_text(encoding="utf-8"), "com.isiniu.demo")
            text = text.replace("isiniu-demo", "${rootArtifactId}")
            write(dest / "AGENTS.md", text)


def single_pom() -> str:
    source = (ROOT / "isiniu-single-demo" / "pom.xml").read_text(encoding="utf-8")
    deps = source.split("<dependencies>", 1)[1].split("</dependencies>", 1)[0]
    build = source.split("<build>", 1)[1].split("</build>", 1)[0]
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.isiniu</groupId>
        <artifactId>isiniu-parent</artifactId>
        <version>${{isiniuParentVersion}}</version>
        <relativePath/>
    </parent>

    <groupId>${{groupId}}</groupId>
    <artifactId>${{rootArtifactId}}</artifactId>
    <version>${{version}}</version>
    <name>${{rootArtifactId}}</name>
    <description>单模块服务。</description>

    <dependencies>{deps}</dependencies>

    <build>{build}</build>
</project>
"""


def simplified_agents(full: str) -> str:
    start = full.index("<!-- MODULE_STRUCTURE_START -->")
    end = full.index("<!-- MODULE_STRUCTURE_END -->") + len("<!-- MODULE_STRUCTURE_END -->")
    replacement = """### 3.4 模块结构（单模块）

本项目是一个 Maven 模块，适合小工具和内部服务。包结构按第 3.3 节，全部放在 `{base}` 下。

- 公共能力依赖 `isiniu-common`，不要复制它的源码
- 鉴权、跨域由 `isiniu-common` 自动配置提供；异步线程池、健康检查放在 `config` / `controller`
- 分层约束由 ArchUnit 检查：controller 不依赖 repository/mapper，service 不依赖 mapper，mapper 不依赖 dto.response
- 出现第二个业务领域、需要隔离 Mapper 和 Model 时，改用模块化单体模板

标杆业务是问题管理，类名以 `Issue` 开头；鉴权代码在 `auth` 包。
"""
    text = full[:start] + replacement + full[end:]
    text = text.replace("| 标杆模块 | `${artifactId}-issue`（问题管理，新代码照它写） |", "| 标杆模块 | `IssueController`（问题管理，新代码照它写） |")
    return text


def build_single(out: Path) -> None:
    reset(out)
    source = ROOT / "isiniu-single-demo"
    write(out / "pom.xml", single_pom())
    write(out / "README.md", (ROOT / "archetype-src" / "single-README.md").read_text(encoding="utf-8"))
    write(out / "CLAUDE.md", "@AGENTS.md\n", escape=False)
    write(out / "AGENTS.md", clean_agents(simplified_agents((ROOT / "AGENTS.md").read_text(encoding="utf-8"))))
    write(out / ".gitignore", (ROOT / "archetype-src" / "gitignore.txt").read_text(encoding="utf-8"))
    write(out / ".github" / "workflows" / "ci.yml", (ROOT / "archetype-src" / "ci.yml").read_text(encoding="utf-8"))
    write(out / "Dockerfile", (source / "Dockerfile").read_text(encoding="utf-8"))
    copy_docs(ROOT / "docs", out / "docs")
    copy_docs(source / "docs", out / "docs")
    for file in (source / "src").rglob("*"):
        if not file.is_file():
            continue
        rel = file.relative_to(source).as_posix()
        text = rewrite_business(file.read_text(encoding="utf-8"), "com.isiniu.single")
        text = text.replace("isiniu-single-demo", "${rootArtifactId}")
        text = text.replace("isiniu_single", "app")
        path = rel.replace("com/isiniu/single", "__packageInPathFormat__")
        write(out / path, text)


def main() -> None:
    build_modular(ROOT / "isiniu-archetype-modular" / "src" / "main" / "resources" / "archetype-resources")
    build_single(ROOT / "isiniu-archetype-single" / "src" / "main" / "resources" / "archetype-resources")


if __name__ == "__main__":
    main()
