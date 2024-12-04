.class public Point
.super java/lang/Object

.field private x I
.field private y I

.method public <init>(II)V
    .limit stack 2
    .limit locals 5
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public move(II)V
    .limit stack 3
    .limit locals 10
    istore 1
    istore 2
    return
.end method
