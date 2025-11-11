#!/bin/bash

# Script de setup do ambiente LaTeX para TCC
# Detecta a distribuição e instala as dependências necessárias

set -e

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}Setup do Ambiente LaTeX para TCC${NC}"
echo -e "${GREEN}================================${NC}"
echo ""

# Detectar distribuição
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$ID
    VERSION=$VERSION_ID
else
    echo -e "${RED}Não foi possível detectar a distribuição Linux${NC}"
    exit 1
fi

echo -e "${YELLOW}Sistema detectado: $OS $VERSION${NC}"
echo ""

# Função para instalar no Arch Linux
install_arch() {
    echo -e "${GREEN}Instalando pacotes para Arch Linux...${NC}"
    sudo pacman -Syu --needed --noconfirm \
        texlive-most \
        texlive-lang \
        texlive-publishers \
        biber \
        zathura \
        zathura-pdf-mupdf \
        make
    echo -e "${GREEN}Instalação concluída!${NC}"
}

# Função para instalar no Debian/Ubuntu
install_debian() {
    echo -e "${GREEN}Instalando pacotes para Debian/Ubuntu...${NC}"
    sudo apt update
    sudo apt install -y \
        texlive-full \
        texlive-lang-portuguese \
        texlive-publishers \
        texlive-fonts-extra \
        biber \
        latexmk \
        zathura \
        make
    echo -e "${GREEN}Instalação concluída!${NC}"
}

# Função para instalar no Fedora
install_fedora() {
    echo -e "${GREEN}Instalando pacotes para Fedora...${NC}"
    sudo dnf install -y \
        texlive-scheme-full \
        latexmk \
        zathura \
        zathura-pdf-mupdf \
        make
    echo -e "${GREEN}Instalação concluída!${NC}"
}

# Selecionar instalador baseado na distribuição
case $OS in
    arch|manjaro|endeavouros)
        install_arch
        ;;
    debian|ubuntu|linuxmint|pop)
        install_debian
        ;;
    fedora|rhel|centos)
        install_fedora
        ;;
    *)
        echo -e "${RED}Distribuição não suportada: $OS${NC}"
        echo -e "${YELLOW}Por favor, instale manualmente:${NC}"
        echo "- TeX Live (completo)"
        echo "- abnTeX2"
        echo "- Zathura (visualizador PDF)"
        echo "- latexmk"
        echo "- make"
        exit 1
        ;;
esac

# Verificar instalações
echo ""
echo -e "${YELLOW}Verificando instalações...${NC}"

check_command() {
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}✓${NC} $1 encontrado"
        return 0
    else
        echo -e "${RED}✗${NC} $1 não encontrado"
        return 1
    fi
}

ALL_OK=true

check_command "pdflatex" || ALL_OK=false
check_command "bibtex" || ALL_OK=false
check_command "latexmk" || ALL_OK=false
check_command "zathura" || ALL_OK=false
check_command "make" || ALL_OK=false

echo ""

# Verificar abnTeX2
echo -e "${YELLOW}Verificando abnTeX2...${NC}"
if kpsewhich abntex2.cls &> /dev/null; then
    echo -e "${GREEN}✓${NC} abnTeX2 encontrado"
else
    echo -e "${RED}✗${NC} abnTeX2 não encontrado"
    ALL_OK=false
    echo -e "${YELLOW}Execute: sudo tlmgr install abntex2${NC}"
fi

echo ""

if [ "$ALL_OK" = true ]; then
    echo -e "${GREEN}✓ Todas as dependências foram instaladas com sucesso!${NC}"
    echo ""
    echo -e "${YELLOW}Próximos passos:${NC}"
    echo "1. Compile o documento: ${GREEN}make pdf${NC}"
    echo "2. Ou use compilação contínua: ${GREEN}make watch${NC}"
    echo "3. Abra o PDF: ${GREEN}make view${NC}"
    echo ""
    echo -e "${YELLOW}Para Neovim, instale o plugin VimTeX:${NC}"
    echo "Consulte o README.md para instruções detalhadas"
else
    echo -e "${RED}✗ Algumas dependências estão faltando${NC}"
    echo "Por favor, instale manualmente os componentes faltantes"
    exit 1
fi

# Criar configuração básica do Zathura (se não existir)
ZATHURA_CONFIG="$HOME/.config/zathura/zathurarc"
if [ ! -f "$ZATHURA_CONFIG" ]; then
    echo ""
    echo -e "${YELLOW}Criando configuração do Zathura...${NC}"
    mkdir -p "$HOME/.config/zathura"
    cat > "$ZATHURA_CONFIG" << 'EOF'
# Configuração do Zathura para trabalho com LaTeX

# Copiar para clipboard ao selecionar
set selection-clipboard clipboard

# Tema escuro (opcional)
set recolor true
set recolor-lightcolor "#1e1e1e"
set recolor-darkcolor "#dcdcdc"

# Recarregamento automático
set recolor-keephue true

# Zoom
set adjust-open "best-fit"
set pages-per-row 1

# Scrolling suave
set scroll-page-aware true
set smooth-scroll true
set scroll-step 100
EOF
    echo -e "${GREEN}✓${NC} Configuração do Zathura criada em $ZATHURA_CONFIG"
fi

echo ""
echo -e "${GREEN}Setup concluído! Bom trabalho com seu TCC! 📚${NC}"
