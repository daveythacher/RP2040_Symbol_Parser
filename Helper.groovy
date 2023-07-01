/* 
 * File:   Helper.groovy
 * Author: David Thacher
 * License: GPL 3.0
 */

// This is a bit of a mess
//  Run on local system only

class Helper {
    def build_list(name, entry_points) {
        def i = 0
        def list = []

        table = []
        symbols = []
        str = name + ".dis"
        read_all_map_symbols(name)

        // This is why we do not use dynamic typing for anything...using loop
        for (int x = 0; x < entry_points.size(); x++)
            list.add(entry_points[x])

        // Move through unique list carefully
        while (i < list.size()) {
            def tmp = get_symbols(list[i])

            //println tmp

            // This is why we do not use dynamic typing for anything...using loop
            for (int x = 0; x < tmp.size(); x++) {
                // Do not add duplicates and always add to the end of the list
                if (!(list.find() { it == tmp[x] }))
                    list.add(tmp[x])
            }

            i++

            //println i + " out of " + list.size()
        }

	    return symbols
    }

    def verify_symbol(file, name) {
        def found = false

        read_all_map_symbols(file)

        table.each() {
            if (name.contains(it[0]) || it[0].contains(name)) {
                found = true
                println "Verified " + name + " is at " + it[1]
            }
        }

        if (!found)
            println ("Did not verify " + name)
    }

    // May do bad things
    private def get_symbols(name) {
        def range = get_range(name)
        def list = []

        // Bail if not found or supported
        if (range[0] == 0 && range[1] == 0)
            return list

        // Advance to subroutine for symbol
        advance_file(range[0])

        if (Integer.valueOf(range[0], 16) < 0x20000000)
            log_symbol(name)

        // Search Assembly for more symbols
        while (true) {
            def line = dis.readLine()

            if (line == null)   // End of file?
                break;
            
            if (line.split(":").size() > 1) {
                def address = line.split(":")[0]

                // Look for new symbol, add if not already seen
                if (line.split("<").size() > 1 && line.split("<")[1].contains(">")) {
                    line = line.split("<")[1].split(">")[0]
                    line = line.split("\\+")[0]
                    list.add(line)
                    list.toUnique()

                    //println line
                }

                // Reached the end of the assembly for the symbols routine
                if (range[1] == Integer.valueOf(address, 16))
                    break;
            }
        }

        return list
    }

    // May do bad things
    //  This is not proven to capture all symbols
    private def read_all_map_symbols(name) {
        table = []
        def file = new File(name + ".elf.map")
        def reader = file.newReader()

        // Align with section time_critical
        while (!reader.readLine().contains("*(.time_critical*)")) {}

        while (true) {
            def line = reader.readLine()

            // Bail out
            if (line == null || line.split("\\s").size() <= 1)
                break;

            // Look for symbols
            if (line.split("\\s")[1].startsWith(".time_critical.")) {
                def tmp = line.split("\\s")[1].split("\\.")
                def symbol_name = tmp[2]
                def symbol_address = reader.readLine().split("\\s+")[1]

                table.add([symbol_name, symbol_address])
            } 
        }

        file = new File(name + ".elf.map")
        reader = file.newReader()

        // Align with section text
        while (!reader.readLine().contains(".text*)")) {}

        while (true) {
            def line = reader.readLine()

            // Bail out
            if (line == null || line.split("\\s").size() <= 1)
                break;

            // Look for symbols
            if (line.split("\\s")[1].startsWith(".text.")) {
                def tmp = line.split("\\s")[1].split("\\.")
                def symbol_name = tmp[2]
                def symbol_address = reader.readLine().split("\\s+")[1]

                table.add([symbol_name, symbol_address])
            } 
        }
    }

    // May do bad things
    private def get_range(name) {
        def found = false
        def result = [0, 0]

        // Give position of current symbol and next symbol in list
        //  This is not proven to work for all symbols
        table.eachWithIndex() { it, i ->
            if ((it[0].contains(name) || name.contains(it[0])) && name != "unlikely") {
                found = true
                result = [it[1].split("0x00000000")[1], Integer.decode(table[i + 1][1])]
            }
        }
        
        if (!found)
            println("Unsupported symbol " + name)
        
        return result
    }

    private def advance_file(start) {
        dis = new File(str).newReader()

        // Look for target symbol
        while(true) {
            def line = dis.readLine()

            // Did not find
            if (line == null)   // End of file?
                break;

            // Found target symbol
            line = line.split(":")
            if (line[0].contains(start))
                break;
        }
    }

    private def log_symbol(symbol) {
        symbols.add(symbol)
    }

    private def table = []
    private def symbols = []
    private BufferedReader dis;
    private def str = ""
}
