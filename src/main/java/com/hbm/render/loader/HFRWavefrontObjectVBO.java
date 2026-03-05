package com.hbm.render.loader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import net.minecraftforge.client.model.obj.TextureCoordinate;
import net.minecraftforge.client.model.obj.Vertex;

public class HFRWavefrontObjectVBO implements IModelCustomNamed {

	class VBOBufferData {
		String name;
		int indexCount = 0; // 修改：改为记录索引数量
		int vertexHandle;
		int uvHandle;
		int normalHandle;
		int indexHandle;    // 新增：EBO (元素缓冲对象) 句柄
	}

	List<VBOBufferData> groups = new ArrayList<VBOBufferData>();

	static int VERTEX_SIZE = 3;
	static int UV_SIZE = 3;

	// 新增：用于唯一标识一个顶点的辅助类 (由 坐标、UV、法线 三者共同决定)
	private static class VertexKey {
		Vertex v;
		TextureCoordinate vt;
		Vertex vn;

		public VertexKey(Vertex v, TextureCoordinate vt, Vertex vn) {
			this.v = v;
			this.vt = vt;
			this.vn = vn;
		}

		// 因为 HFRWavefrontObject 解析时就重用了同一对象的引用，所以可以直接用 == 判断
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			VertexKey that = (VertexKey) o;
			return v == that.v && vt == that.vt && vn == that.vn;
		}

		@Override
		public int hashCode() {
			int result = v != null ? v.hashCode() : 0;
			result = 31 * result + (vt != null ? vt.hashCode() : 0);
			result = 31 * result + (vn != null ? vn.hashCode() : 0);
			return result;
		}
	}

	public HFRWavefrontObjectVBO(HFRWavefrontObject obj) {
		load(obj);
	}

	public void load(HFRWavefrontObject obj) {
		for(S_GroupObject g : obj.groupObjects) {
			VBOBufferData data = new VBOBufferData();
			data.name = g.name;

			// 1. 收集不重复的顶点数据 和 绘制索引
			Map<VertexKey, Integer> uniqueVertices = new LinkedHashMap<VertexKey, Integer>();
			List<Integer> indices = new ArrayList<Integer>();

			for(S_Face face : g.faces) {
				if(face.vertices.length == 3) {
					// 三角形
					for(int i = 0; i < 3; i++) {
						addVertexToIndexList(face, i, uniqueVertices, indices);
					}
				} else if(face.vertices.length == 4) {
					// 四边形 - 剖分为两个三角形
					// 三角形1: 0, 1, 2
					addVertexToIndexList(face, 0, uniqueVertices, indices);
					addVertexToIndexList(face, 1, uniqueVertices, indices);
					addVertexToIndexList(face, 2, uniqueVertices, indices);
					// 三角形2: 0, 2, 3
					addVertexToIndexList(face, 0, uniqueVertices, indices);
					addVertexToIndexList(face, 2, uniqueVertices, indices);
					addVertexToIndexList(face, 3, uniqueVertices, indices);
				}
			}

			data.indexCount = indices.size();

			// 2. 根据唯一顶点的数量创建 Buffer
			FloatBuffer vertexData = BufferUtils.createFloatBuffer(uniqueVertices.size() * VERTEX_SIZE);
			FloatBuffer uvData = BufferUtils.createFloatBuffer(uniqueVertices.size() * UV_SIZE);
			FloatBuffer normalData = BufferUtils.createFloatBuffer(uniqueVertices.size() * VERTEX_SIZE);
			IntBuffer indexData = BufferUtils.createIntBuffer(indices.size());

			// 填充真实的去重顶点数据
			for (VertexKey key : uniqueVertices.keySet()) {
				vertexData.put(new float[] { key.v.x, key.v.y, key.v.z });

				if (key.vt != null) {
					uvData.put(new float[] { key.vt.u, key.vt.v, key.vt.w });
				} else {
					uvData.put(new float[] { 0, 0, 0 });
				}

				if (key.vn != null) {
					normalData.put(new float[] { key.vn.x, key.vn.y, key.vn.z });
				} else {
					normalData.put(new float[] { 0, 0, 0 });
				}
			}

			// 填充索引数据
			for (Integer index : indices) {
				indexData.put(index);
			}

			vertexData.flip();
			uvData.flip();
			normalData.flip();
			indexData.flip();

			// 3. 将 VBO 数据推送到 GPU
			data.vertexHandle = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.vertexHandle);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);

			data.uvHandle = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.uvHandle);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, uvData, GL15.GL_STATIC_DRAW);

			data.normalHandle = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.normalHandle);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalData, GL15.GL_STATIC_DRAW);

			// 新增：将 EBO (索引数据) 推送到 GPU
			data.indexHandle = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, data.indexHandle);
			GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexData, GL15.GL_STATIC_DRAW);

			// 解绑
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

			groups.add(data);
		}
	}

	// 辅助方法：生成索引并去重
	private void addVertexToIndexList(S_Face face, int index, Map<VertexKey, Integer> uniqueVertices, List<Integer> indices) {
		Vertex v = face.vertices[index];
		TextureCoordinate vt = (face.textureCoordinates != null && index < face.textureCoordinates.length) ? face.textureCoordinates[index] : null;
		Vertex vn = (face.vertexNormals != null && index < face.vertexNormals.length) ? face.vertexNormals[index] : null;

		VertexKey key = new VertexKey(v, vt, vn);

		// 如果这个顶点组合已经存在，直接复用它的序号；如果不存在，添加新的
		Integer existingIndex = uniqueVertices.get(key);
		if (existingIndex == null) {
			existingIndex = uniqueVertices.size();
			uniqueVertices.put(key, existingIndex);
		}

		indices.add(existingIndex);
	}

	public void destroy() {
		for(VBOBufferData data : groups) {
			GL15.glDeleteBuffers(data.vertexHandle);
			GL15.glDeleteBuffers(data.uvHandle);
			GL15.glDeleteBuffers(data.normalHandle);
			GL15.glDeleteBuffers(data.indexHandle); // 记得清理 EBO
		}
		groups.clear();
	}

	@Override
	public String getType() {
		return "obj_vbo_indexed";
	}

	private void renderVBO(VBOBufferData data) {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.vertexHandle);
		GL11.glVertexPointer(VERTEX_SIZE, GL11.GL_FLOAT, 0, 0l);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.uvHandle);
		GL11.glTexCoordPointer(UV_SIZE, GL11.GL_FLOAT, 0, 0l);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.normalHandle);
		GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0l);

		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);

		// 修改：绑定 EBO，并使用 glDrawElements 进行索引渲染
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, data.indexHandle);
		GL11.glDrawElements(GL11.GL_TRIANGLES, data.indexCount, GL11.GL_UNSIGNED_INT, 0l);

		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	@Override
	public void renderAll() {
		for(VBOBufferData data : groups) {
			renderVBO(data);
		}
	}

	@Override
	public void renderOnly(String... groupNames) {
		for(VBOBufferData data : groups) {
			for(String name : groupNames) {
				if(data.name.equalsIgnoreCase(name)) {
					renderVBO(data);
				}
			}
		}
	}

	@Override
	public void renderPart(String partName) {
		for(VBOBufferData data : groups) {
			if(data.name.equalsIgnoreCase(partName)) {
				renderVBO(data);
			}
		}
	}

	@Override
	public void renderAllExcept(String... excludedGroupNames) {
		for(VBOBufferData data : groups) {
			boolean skip = false;
			for(String name : excludedGroupNames) {
				if(data.name.equalsIgnoreCase(name)) {
					skip = true;
					break;
				}
			}
			if(!skip) {
				renderVBO(data);
			}
		}
	}

	@Override
	public List<String> getPartNames() {
		List<String> names = new ArrayList<String>();
		for(VBOBufferData data : groups) {
			names.add(data.name);
		}
		return names;
	}
}
