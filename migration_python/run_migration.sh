#!/bin/bash

echo "========================================"
echo "ElektroniCare Migration Tool"
echo "========================================"

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "ERROR: Python 3 is not installed"
    echo "Please install Python 3.7+ and try again"
    exit 1
fi

# Check Python version
python_version=$(python3 -c "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')")
required_version="3.7"

if [ "$(printf '%s\n' "$required_version" "$python_version" | sort -V | head -n1)" != "$required_version" ]; then
    echo "ERROR: Python 3.7+ is required"
    echo "Current version: $python_version"
    exit 1
fi

# Check if requirements are installed
python3 -c "import firebase_admin" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "Installing dependencies..."
    python3 -m pip install -r requirements.txt
    if [ $? -ne 0 ]; then
        echo "ERROR: Failed to install dependencies"
        exit 1
    fi
fi

# Make scripts executable
chmod +x history_migration.py
chmod +x interactive_migration.py

# Run interactive migration
echo "Starting interactive migration tool..."
python3 interactive_migration.py