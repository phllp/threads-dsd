#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>

QT_BEGIN_NAMESPACE
namespace Ui {
class MainWindow;
}
QT_END_NAMESPACE

struct Matrix {
    int rows = 0;
    int cols = 0;
    QVector<QVector<int>> data;
};

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    MainWindow(QWidget *parent = nullptr);
    ~MainWindow();

private:
    Ui::MainWindow *ui;

    Matrix m_;

    bool loadMatrixFromFile(const QString& path, Matrix& out, QString* err = nullptr);
    void printMatrix(const Matrix& m);
    void loadAndPrint();
};
#endif // MAINWINDOW_H
