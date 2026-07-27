import eslint from "@eslint/js";
import tseslint from "typescript-eslint";
import astroParser from "astro-eslint-parser";
import astroPlugin from "eslint-plugin-astro";
import jsxA11y from "eslint-plugin-jsx-a11y";

export default tseslint.config(
  eslint.configs.recommended,
  ...tseslint.configs.recommended,
  ...astroPlugin.configs.recommended,
  jsxA11y.flatConfigs.recommended,
  {
    ignores: ["dist/", ".astro/", "node_modules/"],
  },
  {
    files: ["*.astro"],
    parser: astroParser,
    parserOptions: {
      parser: "@typescript-eslint/parser",
      extraFileExtensions: [".astro"],
    },
  },
  {
    rules: {
      "@typescript-eslint/no-unused-vars": ["error", { argsIgnorePattern: "^_" }],
      "@typescript-eslint/no-explicit-any": "warn",
    },
  },
);
