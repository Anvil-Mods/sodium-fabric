package me.jellysquid.mods.sodium.interop.vanilla.math.matrix;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import me.jellysquid.mods.sodium.util.packed.Normal3b;
import net.minecraft.core.Direction;

@SuppressWarnings("ConstantConditions")
public class MatrixUtil {
    public static int computeNormal(Matrix3f normalMatrix, Direction facing) {
        return ((Matrix3fExtended) (Object) normalMatrix).computeNormal(facing);
    }

    public static Matrix4fExtended getExtendedMatrix(Matrix4f matrix) {
        return (Matrix4fExtended) (Object) matrix;
    }

    public static Matrix3fExtended getExtendedMatrix(Matrix3f matrix) {
        return (Matrix3fExtended) (Object) matrix;
    }

    public static int transformPackedNormal(int norm, Matrix3f matrix) {
        Matrix3fExtended mat = MatrixUtil.getExtendedMatrix(matrix);

        float normX1 = Normal3b.unpackX(norm);
        float normY1 = Normal3b.unpackY(norm);
        float normZ1 = Normal3b.unpackZ(norm);

        float normX2 = mat.transformVecX(normX1, normY1, normZ1);
        float normY2 = mat.transformVecY(normX1, normY1, normZ1);
        float normZ2 = mat.transformVecZ(normX1, normY1, normZ1);

        return Normal3b.pack(normX2, normY2, normZ2);
    }
}
