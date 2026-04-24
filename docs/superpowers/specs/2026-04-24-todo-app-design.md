# Todo App Design

## Overview

A minimal todo application built as a single HTML file. In-memory data only (no persistence). Clean card-style UI on a light gray background.

## Tech Stack

- Single `index.html` file
- Inline `<style>` and `<script>`
- Zero external dependencies
- Pure vanilla JS, no frameworks

## Data Model

JS array of objects in memory:

```js
[{ id: number, text: string, done: boolean }]
```

Data is lost on page refresh. No localStorage, no backend.

## Features

| Feature  | Behavior                                                        |
| -------- | --------------------------------------------------------------- |
| Add      | Input + button (or Enter key). Empty input is rejected silently |
| Complete | Click checkbox to toggle. Done items get strikethrough + gray   |
| Delete   | Click trash button. Removes immediately, no confirmation        |

## Layout

```
┌──────────────────────────────┐
│         待办事项              │  title, centered
├──────────────────────────────┤
│  [input...............] [添加] │  input area
├──────────────────────────────┤
│  ☐ item text             🗑  │  todo card
│  ☑ done item (strike)    🗑  │  completed card
└──────────────────────────────┘
```

- Content area centered, max-width 500px
- Each todo item is a white card with rounded corners (8px) and light shadow

## Style

- Page background: `#f5f5f5`
- Cards: white, `border-radius: 8px`, `box-shadow: 0 1px 3px rgba(0,0,0,0.1)`
- Consistent rounded inputs and buttons
- Completed items: text `#999`, `text-decoration: line-through`

## Interactions

- Enter key in input triggers add (same as clicking button)
- Input is cleared after successful add
- Checkbox toggles `done` state and re-renders
- Delete button removes item from array and re-renders
