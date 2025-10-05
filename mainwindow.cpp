#include "mainwindow.h"
#include "./ui_mainwindow.h"
#include <QFile>
#include <QTextStream>
#include <QMessageBox>
#include <QDebug>


MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent)
    , ui(new Ui::MainWindow)
{
    ui->setupUi(this);
    loadAndPrint();
}

MainWindow::~MainWindow()
{
    delete ui;
}

bool MainWindow::loadMatrixFromFile(const QString& path, Matrix& out, QString* err) {
    QFile f(path);
    if (!f.open(QIODevice::ReadOnly | QIODevice::Text)) {
        if (err) *err = QStringLiteral("Não foi possível abrir o arquivo: %1").arg(f.errorString());
        return false;
    }

    QTextStream in(&f);

    auto readInt = [&](int& target, const char* what) -> bool {
        if (in.atEnd()) {
            if (err) *err = QStringLiteral("Fim inesperado ao ler %1.").arg(what);
            return false;
        }
        QString tok;
        in >> tok;
        bool ok = false;
        int val = tok.toInt(&ok, 10);
        if (!ok) {
            if (err) *err = QStringLiteral("Valor inválido para %1: \"%2\"").arg(what, tok);
            return false;
        }
        target = val;
        return true;
    };

    if (!readInt(out.rows, "quantidade de linhas")) return false;
    if (!readInt(out.cols, "quantidade de colunas")) return false;

    if (out.rows <= 0 || out.cols <= 0) {
        if (err) *err = QStringLiteral("Dimensões inválidas: %1 x %2.").arg(out.rows).arg(out.cols);
        return false;
    }

    out.data = QVector<QVector<int>>(out.rows, QVector<int>(out.cols, 0));

    for (int r = 0; r < out.rows; ++r) {
        for (int c = 0; c < out.cols; ++c) {
            if (!readInt(out.data[r][c], "elemento da matriz")) {
                if (err) *err = QStringLiteral("%1 (posição %2,%3).").arg(*err).arg(r).arg(c);
                return false;
            }
        }
    }

    if (in.status() != QTextStream::Ok && in.status() != QTextStream::ReadPastEnd) {
        if (err) *err = QStringLiteral("Falha de leitura (status %1).").arg(int(in.status()));
        return false;
    }
    return true;
}


void MainWindow::printMatrix(const Matrix& m) {
    QTextStream out(stdout);
    out << "Matriz (" << m.rows << " x " << m.cols << "):\n";
    for (int r = 0; r < m.rows; ++r) {
        for (int c = 0; c < m.cols; ++c) {
            out << m.data[r][c];
            if (c + 1 < m.cols) out << '\t';
        }
        out << '\n';
    }
    out.flush();

    qInfo().noquote() << "Leitura concluída. Dimensões:" << m.rows << "x" << m.cols;
}

void MainWindow::loadAndPrint() {
    // ARQUIVO SENDO CARREGADO:
    const QString path = QStringLiteral("malha-exemplo-1.txt");

    QString err;
    if (!loadMatrixFromFile(path, m_, &err)) {
        QMessageBox::critical(this, tr("Erro ao ler matriz"), err);
        return;
    }
    printMatrix(m_);
}
