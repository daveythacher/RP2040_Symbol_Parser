/* 
 * File:   main.groovy
 * Author: David Thacher
 * License: GPL 3.0
 */
import Helper

def helper = new Helper()
def entry_points = [ "work", "dma_isr0", "dma_isr1", "process" ]

helper.build_list("led_TEST4", entry_points).each() { it2 ->
    println "Found " + it2
}

helper.verify_symbol("led_TEST4", "multicore_fifo_push_blocking")
helper.verify_symbol("led_TEST4", "dma_isr0")
