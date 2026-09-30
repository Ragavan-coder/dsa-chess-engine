import os, re

# Gather all Java files
java_files = []
for root, dirs, files in os.walk('src'):
    for file in files:
        if file.endswith('.java'):
            java_files.append(os.path.join(root, file))

code = ""
for f in java_files:
    with open(f, 'r', encoding='utf-8') as file:
        code += file.read() + "\n"

# Strip comments
code = re.sub(r'/\*.*?\*/', '', code, flags=re.DOTALL)
code = re.sub(r'//.*', '', code)

# Extract imports and remove them from the body
imports = set(re.findall(r'^import\s+.*?;', code, flags=re.MULTILINE))
code = re.sub(r'^import\s+.*?;[ \t]*\n?', '', code, flags=re.MULTILINE)

# Remove 'public' from classes/enums (except Main) so it can compile as one file
code = re.sub(r'\bpublic\s+(class|enum|interface)\s+([A-Za-z0-9_]+)', lambda m: f'public {m.group(1)} {m.group(2)}' if m.group(2) == 'Main' else f'{m.group(1)} {m.group(2)}', code)

# Safe minification of long variable and method names
replacements = {
    'board': 'bd',
    'squares': 'sqs',
    'whiteToMove': 'wtm',
    'zobristHash': 'zh',
    'enPassantSquare': 'eps',
    'halfMoveClock': 'hmc',
    'fullMoveNumber': 'fmn',
    'moveHistory': 'mh',
    'whiteKingMoved': 'wkm',
    'blackKingMoved': 'bkm',
    'whiteKingsideRookMoved': 'wkrm',
    'whiteQueensideRookMoved': 'wqrm',
    'blackKingsideRookMoved': 'bkrm',
    'blackQueensideRookMoved': 'bqrm',
    'pseudoLegal': 'pl',
    'legalMoves': 'lm',
    'positionHistory': 'ph',
    'capturedPiece': 'cp',
    'promotionPiece': 'pp',
    'movedPiece': 'mp',
    'moveType': 'mt',
    'generateLegalMoves': 'glm',
    'generatePseudoLegalMoves': 'gplm',
    'makeMove': 'mkMv',
    'undoMove': 'unMv',
    'isInCheck': 'inChk'
}

for old, new in replacements.items():
    code = re.sub(r'\b' + old + r'\b', new, code)

# Remove excess empty lines and leading whitespace to make it as small as possible
lines = [line.strip() for line in code.split('\n') if line.strip()]
code = '\n'.join(lines)

final_code = '\n'.join(imports) + '\n\n' + code

# Write the final unified and minified file
with open('Main.java', 'w', encoding='utf-8') as f:
    f.write(final_code)
print("Main.java created successfully!")
