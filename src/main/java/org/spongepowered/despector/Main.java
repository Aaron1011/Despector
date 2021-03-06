/*
 * The MIT License (MIT)
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.despector;

import com.google.common.collect.Lists;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.io.DirectoryWalker;
import org.spongepowered.despector.ast.io.JarWalker;
import org.spongepowered.despector.ast.io.SingularClassLoader;
import org.spongepowered.despector.ast.io.emitter.SourceEmitter;
import org.spongepowered.despector.ast.type.TypeEntry;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java -jar Despector.jar [sources...] [destination]");
            return;
        }
        List<String> sources = Lists.newArrayList();
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].startsWith("-")) {
                // TODO parse flags
            } else {
                sources.add(args[i]);
            }
        }
        String destination = args[args.length - 1];
        Path output = Paths.get(destination).toAbsolutePath();
        if (!Files.exists(output)) {
            Files.createDirectories(output);
        }

        SourceSet source = new SourceSet();
        for (String s : sources) {
            Path path = Paths.get(s);
            if (!Files.exists(path)) {
                System.err.println("Unknown source: " + path.toAbsolutePath().toString());
            } else if (s.endsWith(".jar")) {
                JarWalker walker = new JarWalker(path);
                walker.walk(source);
            } else if (Files.isDirectory(path)) {
                DirectoryWalker walker = new DirectoryWalker(path);
                try {
                    walker.walk(source);
                } catch (IOException e) {
                    System.err.println("Error while walking directory: " + path.toAbsolutePath().toString());
                    e.printStackTrace();
                }
            } else if (s.endsWith(".class")) {
                SingularClassLoader.instance.load(path, source);
            } else {
                System.err.println("Unknown source type: " + path.toAbsolutePath().toString() + " must be jar or directory");
            }
        }

        if (source.getAllClasses().isEmpty()) {
            System.err.println("No sources found.");
            return;
        }

        for (TypeEntry type : source.getAllClasses()) {
            Path out = output.resolve(type.getName() + ".java");
            if (!Files.exists(out.getParent())) {
                Files.createDirectories(out.getParent());
            }
            try (FileWriter writer = new FileWriter(out.toFile())) {
                SourceEmitter emitter = new SourceEmitter(writer);
                emitter.emitType(type);
            }
        }

    }

}
