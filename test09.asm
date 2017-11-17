.data                         # BEGIN Data Segment
data.newline:      .asciiz       "\n"
data.floatquery:   .asciiz       "float?"
data.intquery:     .asciiz       "int?"
data.trueString:   .asciiz       "true"
data.falseString:  .asciiz       "false"
                              # END Data Segment
.text                         # BEGIN Code Segment
func.printBool:
lw $a0, 0($sp)
beqz $a0, label.printBool.loadFalse
la $a0, data.trueString
j label.printBool.join
label.printBool.loadFalse:
la $a0, data.falseString
label.printBool.join:
li   $v0, 4
syscall
jr $ra
func.printFloat:
l.s  $f12, 0($sp)
li   $v0,  2
syscall
jr $ra
func.printInt:
lw   $a0, 0($sp)
li   $v0, 1
syscall
jr $ra
func.println:
la   $a0, data.newline
li   $v0, 4
syscall
jr $ra
func.readFloat:
la   $a0, data.floatquery
li   $v0, 4
syscall
li   $v0, 6
syscall
mfc1 $v0, $f0
jr $ra
func.readInt:
la   $a0, data.intquery
li   $v0, 4
syscall
li   $v0, 5
syscall
jr $ra
.text                         # BEGIN Crux Program
main: 
subu $sp, $sp, 8
sw $fp, 0($sp)
sw $ra, 4($sp)
addi $fp, $sp, 8
li $t0, 1
subu $sp, $sp, 4
sw $t0, 0($sp)
li $t0, 2
subu $sp, $sp, 4
sw $t0, 0($sp)
lw $t1, 0($sp)
addiu $sp, $sp, 4
lw $t0, 0($sp)
addiu $sp, $sp, 4
add $t2, $t1, $t0
subu $sp, $sp, 4
sw $t2, 0($sp)
jal func.printInt
addi $sp, $sp, 4
jal func.println
li.s $f0, 0.5
subu $sp, $sp, 4
swc1 $f0, 0($sp)
li.s $f0, 0.125
subu $sp, $sp, 4
swc1 $f0, 0($sp)
lwc1 $f1, 0($sp)
addiu $sp, $sp, 4
lwc1 $f0, 0($sp)
addiu $sp, $sp, 4
sub.s $f2, $f0, $f1
subu $sp, $sp, 4
swc1 $f2, 0($sp)
jal func.printFloat
addi $sp, $sp, 4
label.0: 
li    $v0, 10
syscall
                              # END Code Segment
