/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.pathnode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PathNode{

	private final List<PathNode> children = new ArrayList<>();

	protected PathNode parent;
	protected String value;

	@SuppressWarnings("unchecked")
	public <P extends PathNode> P branch(Supplier<P> childSupplier, String childName){
		return children.stream()
				.filter(child -> child.value.equals(childName))
				.findAny()
				.map(child -> (P)child)
				.orElseGet(() -> {
					P child = childSupplier.get();
					child.parent = this;
					child.value = childName;
					children.add(child);
					return child;
				});
	}

	public PathNode leaf(String childName){
		PathNode child = new PathNode();
		child.parent = this;
		child.value = childName;
		children.add(child);
		return child;
	}

	public List<PathNode> paths(){
		List<PathNode> paths = new ArrayList<>();
		paths.add(this);
		children.stream()
				.map(PathNode::paths)
				.forEach(paths::addAll);
		return paths;
	}

	public String[] toStringArray(){
		return nodesAfter(null, this).stream()
				.map(PathNode::getValue)
				.toArray(String[]::new);
	}

	public static String toSlashedString(List<PathNode> nodes, boolean includeLeadingSlash){
		String prefix = includeLeadingSlash ? "/" : "";
		return joinNodes(nodes, prefix, "/", "");
	}

	public String toSlashedString(){
		return join("/", "/", "");
	}

	public String toSlashedStringWithTrailingSlash(){
		return join("/", "/", "/");
	}

	/**
	 * prefix and suffix are empty strings
	 *
	 * @param delimiter delimiter
	 * @return joined string
	 */
	public String join(String delimiter){
		return join("", delimiter, "");
	}

	public String join(String prefix, String delimiter, String suffix){
		return joinNodes(nodesAfter(null, this), prefix, delimiter, suffix);
	}

	public static List<PathNode> nodesAfter(PathNode after, PathNode through){
		List<PathNode> nodes = new ArrayList<>();
		PathNode cursor = through;
		while(cursor != after && cursor != null && cursor.value != null){
			nodes.add(cursor);
			cursor = cursor.parent;
		}
		Collections.reverse(nodes);
		return nodes;
	}

	public String toSlashedStringAfter(PathNode after, boolean includeLeadingSlash){
		return toSlashedString(nodesAfter(after, this), includeLeadingSlash);
	}

	public static boolean isLeaf(PathNode pathNode){
		return !pathNode.paths().isEmpty();
	}

	@Override
	public int hashCode(){
		return toSlashedString().hashCode();
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(!(obj instanceof PathNode)){
			return false;
		}
		PathNode other = (PathNode)obj;
		return Objects.equals(toSlashedString(), other.toSlashedString());
	}

	// purposefully not usable to avoid unwanted dependencies
	@Override
	public String toString(){
		throw new RuntimeException("PathNode::toString is unusable to avoid unwanted dependencies. PathNode.value="
				+ value);
	}

	public String getValue(){
		return value;
	}

	public static PathNode parse(String path){
		Iterable<Path> pathParts = Path.of(path)::iterator;
		PathNode pathNode = new PathNode();
		for(Path pathPart : pathParts){
			pathNode = pathNode.branch(PathNode::new, pathPart.toString());
		}
		return pathNode;
	}

	private static String joinNodes(List<PathNode> nodes, String prefix, String delimiter, String suffix){
		return nodes.stream()
				.map(pathNode -> pathNode.value)
				.collect(Collectors.joining(delimiter, prefix, suffix));
	}

}
