import { readdirSync, readFileSync } from "node:fs";
import { dirname, extname, join, relative, resolve, sep } from "node:path";
import { fileURLToPath } from "node:url";

const sourceRoot = fileURLToPath(new URL("../src", import.meta.url));
const extensions = ["", ".ts", ".tsx", ".js", ".jsx", ".css", ".png", ".webp", ".svg"];
const errors = [];

function filesIn(directory) {
  return readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const target = join(directory, entry.name);
    return entry.isDirectory() ? filesIn(target) : [target];
  });
}

for (const file of filesIn(sourceRoot)) {
  if (![".ts", ".tsx", ".css"].includes(extname(file))) continue;

  const content = readFileSync(file, "utf8");
  const paths = content.matchAll(/["'](\.{1,2}\/[^"'\r\n]+)["']/g);

  for (const match of paths) {
    const target = resolve(dirname(file), match[1]);
    const parts = relative(sourceRoot, target).split(sep);
    let directory = sourceRoot;

    for (const [index, part] of parts.entries()) {
      const names = readdirSync(directory);
      const candidates = index === parts.length - 1
        ? extensions.map((extension) => `${part}${extension}`)
        : [part];
      const found = candidates.find((candidate) => names.includes(candidate));

      if (!found) {
        errors.push(`${relative(sourceRoot, file)}: ${match[1]}`);
        break;
      }

      directory = join(directory, found);
    }
  }
}

if (errors.length > 0) {
  console.error(`Rutas de importación con mayúsculas incorrectas o inexistentes:\n${errors.join("\n")}`);
  process.exitCode = 1;
}
