package at.ac.tuwien.ifs.sge.game.risk.util;

public enum PriestLogic {

  FALSE(-1),
  UNKNOWN(0),
  TRUE(1);


  private int internalValue;

  PriestLogic(int internalValue) {
    this.internalValue = internalValue;
  }

  public static PriestLogic fromBoolean(boolean bool) {
    if (bool) {
      return TRUE;
    }
    return FALSE;
  }

  private static PriestLogic invertFromInternalValue(int internalValue) {
    if (internalValue < 0) {
      return FALSE;
    } else if (internalValue == 0) {
      return UNKNOWN;
    }
    return TRUE;
  }

  private static PriestLogic not(int internalValue) {
    return invertFromInternalValue(-1 * internalValue);
  }

  public static PriestLogic not(PriestLogic a) {
    return not(a.internalValue);
  }

  public static PriestLogic not(boolean a) {
    return fromBoolean(!a);
  }

  public static PriestLogic and(PriestLogic a, PriestLogic b) {
    return and(a.internalValue, b.internalValue);
  }

  public static PriestLogic and(PriestLogic a, boolean b) {
    return and(a, fromBoolean(b));
  }

  public static PriestLogic and(boolean a, PriestLogic b) {
    return and(fromBoolean(a), b);
  }

  public static PriestLogic and(boolean a, boolean b) {
    return fromBoolean(a && b);
  }


  private static PriestLogic and(int a, int b) {
    return invertFromInternalValue(Math.min(a, b));
  }

  public static PriestLogic or(PriestLogic a, PriestLogic b) {
    return or(a.internalValue, b.internalValue);
  }

  public static PriestLogic or(PriestLogic a, boolean b) {
    return or(a, fromBoolean(b));
  }

  public static PriestLogic or(boolean a, PriestLogic b) {
    return or(fromBoolean(a), b);
  }

  public static PriestLogic or(boolean a, boolean b) {
    return fromBoolean(a || b);
  }

  private static PriestLogic or(int a, int b) {
    return invertFromInternalValue(Math.max(a, b));
  }

  public static PriestLogic implies(PriestLogic a, PriestLogic b) {
    return implies(a.internalValue, b.internalValue);
  }

  public static PriestLogic implies(PriestLogic a, boolean b) {
    return implies(a, fromBoolean(b));
  }

  public static PriestLogic implies(boolean a, PriestLogic b) {
    return implies(fromBoolean(a), b);
  }

  public static PriestLogic implies(boolean a, boolean b) {
    return fromBoolean(!a || b);
  }

  private static PriestLogic implies(int a, int b) {
    return or(-1 * a, b);
  }

  private static PriestLogic xor(int a, int b) {
    return or(and(a, -1 * b), and(-1 * a, b));
  }

  public static PriestLogic xor(PriestLogic a, PriestLogic b) {
    return xor(a.internalValue, b.internalValue);
  }

  public static PriestLogic xor(PriestLogic a, boolean b) {
    return xor(a, fromBoolean(b));
  }

  public static PriestLogic xor(boolean a, PriestLogic b) {
    return xor(fromBoolean(a), b);
  }

  public static PriestLogic xor(boolean a, boolean b) {
    return fromBoolean(a ^ b);
  }

  private static PriestLogic equivalence(int a, int b) {
    return and(implies(a, b), implies(b, a));
  }

  public static PriestLogic equivalence(PriestLogic a, PriestLogic b) {
    return equivalence(a.internalValue, b.internalValue);
  }

  public static PriestLogic equivalence(PriestLogic a, boolean b) {
    return equivalence(a, fromBoolean(b));
  }

  public static PriestLogic equivalence(boolean a, PriestLogic b) {
    return equivalence(fromBoolean(a), b);
  }

  public static PriestLogic equivalence(boolean a, boolean b) {
    return fromBoolean(a == b);
  }

  public static PriestLogic nand(PriestLogic a, PriestLogic b) {
    return nand(a.internalValue, b.internalValue);
  }

  public static PriestLogic nand(PriestLogic a, boolean b) {
    return nand(a, fromBoolean(b));
  }

  public static PriestLogic nand(boolean a, PriestLogic b) {
    return nand(fromBoolean(a), b);
  }

  public static PriestLogic nand(boolean a, boolean b) {
    return fromBoolean(!(a && b));
  }

  private static PriestLogic nand(int a, int b) {
    return not(and(a, b));
  }

  public static PriestLogic nor(PriestLogic a, PriestLogic b) {
    return nor(a.internalValue, b.internalValue);
  }

  public static PriestLogic nor(PriestLogic a, boolean b) {
    return nor(a, fromBoolean(b));
  }

  public static PriestLogic nor(boolean a, PriestLogic b) {
    return nor(fromBoolean(a), b);
  }

  public static PriestLogic nor(boolean a, boolean b) {
    return fromBoolean(!(a || b));
  }

  private static PriestLogic nor(int a, int b) {
    return not(or(a, b));
  }

  public static PriestLogic maybe(PriestLogic a, PriestLogic b) {
    if (a == UNKNOWN || b == UNKNOWN) {
      return UNKNOWN;
    }
    if (a == b) {
      return a;
    }
    return UNKNOWN;

  }


  public static boolean possible(PriestLogic a) {
    return a != FALSE;
  }

  public static boolean impossible(PriestLogic a) {
    return a == FALSE;
  }

  public static boolean certain(PriestLogic a) {
    return a != UNKNOWN;
  }

  public static boolean uncertain(PriestLogic a) {
    return a == UNKNOWN;
  }

  public static boolean falsifiable(PriestLogic a) {
    return a != TRUE;
  }

  public static boolean valid(PriestLogic a) {
    return a == TRUE;
  }

}
