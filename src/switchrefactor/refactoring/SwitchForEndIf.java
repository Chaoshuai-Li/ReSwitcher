package switchrefactor.refactoring;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.YieldStatement;

public class SwitchForEndIf {

	public static boolean checkEndIf(IfStatement ifTemp, boolean labelTemp) {
		List<Statement> list = new ArrayList<>();
		while (true) {
			if (ifTemp.getThenStatement() != null) {
				list.add(ifTemp.getThenStatement());
			} else {
				return false;
			}
			if (ifTemp.getElseStatement() != null) {
				Statement sTemp = ifTemp.getElseStatement();
				if (sTemp instanceof IfStatement) {
					ifTemp = (IfStatement) sTemp;
				} else {
					list.add(sTemp);
					break;
				}
			} else {
				return false;
			}
		}
		for (Statement s : list) {
			if (s instanceof BreakStatement || s instanceof ReturnStatement
					|| s instanceof ThrowStatement
					|| s instanceof ContinueStatement) {
				continue;
			} else if (s instanceof IfStatement) {
				boolean label = checkEndIf((IfStatement) s, labelTemp);
				if (label) {
					continue;
				} else {
					return false;
				}
			} else if (s instanceof TryStatement) {
				boolean label = checkEndTry((TryStatement) s);
				if (label) {
					continue;
				} else {
					return false;
				}
			} else if (s instanceof Block) {
				Block blockTemp = (Block) s;
				int index = blockTemp.statements().size();
				if (index == 0) {
					return false;
				} else {
					Statement sTemp = (Statement) blockTemp.statements()
							.get(index - 1);
					if (sTemp instanceof BreakStatement
							|| sTemp instanceof ReturnStatement
							|| sTemp instanceof ThrowStatement
							|| sTemp instanceof ContinueStatement) {
						continue;
					} else if (sTemp instanceof IfStatement) {
						boolean label = checkEndIf((IfStatement) sTemp,
								labelTemp);
						if (label) {
							continue;
						} else {
							return false;
						}
					} else if (sTemp instanceof TryStatement) {
						boolean label = checkEndTry((TryStatement) sTemp);
						if (label) {
							continue;
						} else {
							return false;
						}
					} else if (labelTemp) {
						return false;
					}
				}
			} else if (labelTemp) {
				return false;
			}
		}
		if (list.size() == 0) {
			return false;
		}

		SwitchRefactoring.deleteIfBreakLabel = true;
		return true;
	}

	public static int analyEndIf(IfStatement ifTemp) {

		boolean firstCheck = true;
		boolean nextCheck = true;
		int flagMethod = 0;
		int flagAssign = 0;
		int flagThrow = 0;

		ArrayList<Expression> listExpressions = new ArrayList<Expression>();
		ArrayList<ReturnStatement> listStatements = new ArrayList<ReturnStatement>();
		ArrayList<Statement> list = getIfList(ifTemp);
		for (Statement s : list) {
			if (s instanceof ExpressionStatement)
				listExpressions.add(((ExpressionStatement) s).getExpression());
			if (s instanceof YieldStatement)
				listExpressions.add(((YieldStatement) s).getExpression());
			if (s instanceof ReturnStatement)
				listStatements.add((ReturnStatement) s);
			if (s instanceof ThrowStatement)
				flagThrow++;
		}

		if (list.size() == listExpressions.size() + flagThrow) {
			for (Expression e : listExpressions) {
				if (e instanceof MethodInvocation) {
					flagMethod++;
				}
				if (e instanceof Assignment) {
					flagAssign++;
				}
			}
		} else if (list.size() == listStatements.size() + flagThrow) {
			for (ReturnStatement r : listStatements) {
				if (r.getExpression() == null) {
					return 3;
				}
			}
			return 2;
		} else {
			return 3;
		}

		if (flagAssign == listExpressions.size() + flagThrow) {
			Assignment aTemp = null, aTemp1 = null;
			for (Expression e : listExpressions) {
				if (firstCheck) {
					aTemp = (Assignment) e;
					if (aTemp.getOperator() != Assignment.Operator.ASSIGN) {
						return 3;
					}
					firstCheck = false;
				} else {
					aTemp1 = (Assignment) e;
					if (aTemp1.getOperator() != Assignment.Operator.ASSIGN) {
						return 3;
					}
					if (!aTemp.getLeftHandSide().toString()
							.equals(aTemp1.getLeftHandSide().toString())) {
						return 3;
					}
				}
			}
			return 1;
		} else if (flagMethod == listExpressions.size() + flagThrow) {
			MethodInvocation mTemp = null, mTemp1 = null;
			for (Expression e : listExpressions) {
				if (nextCheck) {
					mTemp = (MethodInvocation) e;
					if (mTemp.arguments().size() == 0
							|| mTemp.arguments().size() > 1) {
						return 3;
					}
					nextCheck = false;
				} else {
					mTemp1 = (MethodInvocation) e;
					if (mTemp1.arguments().size() == 0
							|| mTemp1.arguments().size() > 1) {
						return 3;
					}
					if (!(mTemp.getName().toString()
							.equals(mTemp1.getName().toString()))) {
						return 3;
					}
					if ((mTemp1.getExpression() == null
							&& mTemp.getExpression() == null)
							|| (mTemp1.getExpression() != null
									&& mTemp.getExpression() != null)) {
						if (mTemp.getExpression() != null) {
							if (!mTemp.getExpression().toString().equals(
									mTemp1.getExpression().toString())) {
								return 3;
							}
						}
					} else {
						return 3;
					}
				}
			}
			return 0;
		} else {
			return 3;
		}
	}

	public static Statement getAnalyEndIf(IfStatement ifTemp) {

		ArrayList<Statement> list = getIfList(ifTemp);
		for (Statement s : list) {
			if (s instanceof ExpressionStatement)
				return s;
			if (s instanceof YieldStatement)
				return s;
			if (s instanceof ReturnStatement)
				return s;
			if (s instanceof ThrowStatement)
				continue;
		}
		return null;
	}

	private static ArrayList<Statement> getIfList(IfStatement ifTemp) {
		ArrayList<Statement> workList = new ArrayList<Statement>();
		ArrayList<Statement> list = new ArrayList<>();
		while (true) {
			list.add(ifTemp.getThenStatement());
			Statement sTemp = ifTemp.getElseStatement();
			if (sTemp instanceof IfStatement) {
				ifTemp = (IfStatement) sTemp;
			} else {
				list.add(sTemp);
				break;
			}
		}
		for (Statement s : list) {
			if (s instanceof BreakStatement) {
				workList.add(s);
				continue;
			} else if (s instanceof ThrowStatement) {
				workList.add(s);
				continue;
			} else if (s instanceof ContinueStatement) {
				workList.add(s);
				continue;
			} else if (s instanceof ReturnStatement) {
				workList.add(s);
				continue;
			} else if (s instanceof IfStatement) {
				workList.addAll(getIfList((IfStatement) s));
			} else if (s instanceof Block) {
				Block blockTemp = (Block) s;
				int index = blockTemp.statements().size();
				Statement sTemp = (Statement) blockTemp.statements()
						.get(index - 1);
				if (sTemp instanceof BreakStatement) {
					if ((index - 1) == 0) {
						workList.add(sTemp);
					} else {
						sTemp.delete();
						sTemp = (Statement) blockTemp.statements()
								.get(index - 2);
						workList.add(sTemp);
					}
				} else if (sTemp instanceof ReturnStatement) {
					workList.add(sTemp);
				} else if (s instanceof ContinueStatement) {
					workList.add(s);
					continue;
				} else if (sTemp instanceof ThrowStatement) {
					workList.add(sTemp);
				} else if (sTemp instanceof IfStatement) {
					workList.addAll(getIfList((IfStatement) sTemp));
				} else {
					workList.add(sTemp);
				}
			} else {
				workList.add(s);
			}
		}
		return workList;
	}

	@SuppressWarnings("unchecked")
	public static void refactorEndIfM(IfStatement ifTemp) {
		AST ast = SwitchRefactoring.astFlag;
		while (true) {
			Statement s = ifTemp.getThenStatement();
			if (!(s instanceof ThrowStatement)) {
				if (s instanceof ExpressionStatement
						|| s instanceof YieldStatement) {
					Expression expre = null;
					if (s instanceof ExpressionStatement) {
						expre = ((ExpressionStatement) s).getExpression();
					} else {
						expre = ((YieldStatement) s).getExpression();
					}
					Expression eTemp = (Expression) ((MethodInvocation) expre)
							.arguments().get(0);
					YieldStatement ys = ast.newYieldStatement();
					eTemp.delete();
					Expression expressionTemp = eTemp;
					ys.setExpression(expressionTemp);
					s.delete();
					ifTemp.setThenStatement(ys);
				} else if (s instanceof Block) {
					Block block = (Block) s;
					int index = block.statements().size() - 1;
					Statement sTemp = (Statement) block.statements().get(index);
					if (sTemp instanceof IfStatement) {
						refactorEndIfM((IfStatement) sTemp);
					} else if (sTemp instanceof ExpressionStatement
							|| sTemp instanceof YieldStatement) {
						Expression expre = null;
						if (sTemp instanceof ExpressionStatement) {
							expre = ((ExpressionStatement) sTemp)
									.getExpression();
						} else {
							expre = ((YieldStatement) sTemp).getExpression();
						}
						Expression eTemp = (Expression) ((MethodInvocation) expre)
								.arguments().get(0);
						eTemp.delete();
						Expression expressionTemp = eTemp;
						YieldStatement ys = ast.newYieldStatement();
						ys.setExpression(expressionTemp);
						sTemp.delete();
						block.statements().add(ys);
					}
				} else if (s instanceof IfStatement) {
					refactorEndIfM((IfStatement) s);
				}
			}
			Statement sTemp = ifTemp.getElseStatement();
			if (sTemp instanceof IfStatement) {
				ifTemp = (IfStatement) sTemp;
			} else {
				if (!(sTemp instanceof ThrowStatement)) {
					if (sTemp instanceof ExpressionStatement
							|| sTemp instanceof YieldStatement) {
						Expression expre = null;
						if (sTemp instanceof ExpressionStatement) {
							expre = ((ExpressionStatement) sTemp)
									.getExpression();
						} else {
							expre = ((YieldStatement) sTemp).getExpression();
						}
						Expression eTemp = (Expression) ((MethodInvocation) expre)
								.arguments().get(0);
						eTemp.delete();
						Expression expressionTemp = eTemp;
						YieldStatement ys = ast.newYieldStatement();
						ys.setExpression(expressionTemp);
						sTemp.delete();
						ifTemp.setThenStatement(ys);
					} else if (sTemp instanceof Block) {
						Block block = (Block) sTemp;
						int index = block.statements().size() - 1;
						Statement sTempTemp = (Statement) block.statements()
								.get(index);
						if (sTempTemp instanceof IfStatement) {
							refactorEndIfM((IfStatement) sTempTemp);
						} else if (sTempTemp instanceof ExpressionStatement
								|| sTempTemp instanceof YieldStatement) {
							Expression expre = null;
							if (sTempTemp instanceof ExpressionStatement) {
								expre = ((ExpressionStatement) sTempTemp)
										.getExpression();
							} else {
								expre = ((YieldStatement) sTempTemp)
										.getExpression();
							}
							Expression eTemp = (Expression) ((MethodInvocation) expre)
									.arguments().get(0);
							eTemp.delete();
							Expression expressionTemp = eTemp;
							YieldStatement ys = ast.newYieldStatement();
							ys.setExpression(expressionTemp);
							sTempTemp.delete();
							block.statements().add(ys);
						}
					} else if (sTemp instanceof IfStatement) {
						refactorEndIfM((IfStatement) sTemp);
					}
				}
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void refactorEndIfA(IfStatement ifTemp) {
		AST ast = SwitchRefactoring.astFlag;
		while (true) {
			Statement s = ifTemp.getThenStatement();
			if (!(s instanceof ThrowStatement)) {
				if (s instanceof ExpressionStatement
						|| s instanceof YieldStatement) {
					Expression expre = null;
					if (s instanceof ExpressionStatement) {
						expre = ((ExpressionStatement) s).getExpression();
					} else {
						expre = ((YieldStatement) s).getExpression();
					}
					Expression eTemp = (Expression) ((Assignment) expre)
							.getRightHandSide();
					Expression expressionTemp = eTemp;
					YieldStatement ys = ast.newYieldStatement();
					ys.setExpression((Expression) ASTNode.copySubtree(ast,
							expressionTemp));
					s.delete();
					ifTemp.setThenStatement(ys);
				} else if (s instanceof Block) {
					Block block = (Block) s;
					int index = block.statements().size() - 1;
					Statement sTemp = (Statement) block.statements().get(index);
					if (sTemp instanceof IfStatement) {
						refactorEndIfA((IfStatement) sTemp);
					} else if (sTemp instanceof ExpressionStatement
							|| sTemp instanceof YieldStatement) {
						Expression expre = null;
						if (sTemp instanceof ExpressionStatement) {
							expre = ((ExpressionStatement) sTemp)
									.getExpression();
						} else {
							expre = ((YieldStatement) sTemp).getExpression();
						}
						Expression eTemp = (Expression) ((Assignment) expre)
								.getRightHandSide();
						Expression expressionTemp = eTemp;
						YieldStatement ys = ast.newYieldStatement();
						ys.setExpression((Expression) ASTNode.copySubtree(ast,
								expressionTemp));
						sTemp.delete();
						block.statements().add(ys);
					}
				} else if (s instanceof IfStatement) {
					refactorEndIfA((IfStatement) s);
				}
			}
			Statement sTemp = ifTemp.getElseStatement();
			if (sTemp instanceof IfStatement) {
				ifTemp = (IfStatement) sTemp;
			} else {
				if (!(sTemp instanceof ThrowStatement)) {
					if (sTemp instanceof ExpressionStatement
							|| sTemp instanceof YieldStatement) {
						Expression expre = null;
						if (sTemp instanceof ExpressionStatement) {
							expre = ((ExpressionStatement) sTemp)
									.getExpression();
						} else {
							expre = ((YieldStatement) sTemp).getExpression();
						}
						Expression eTemp = (Expression) ((Assignment) expre)
								.getRightHandSide();
						Expression expressionTemp = eTemp;
						YieldStatement ys = ast.newYieldStatement();
						ys.setExpression((Expression) ASTNode.copySubtree(ast,
								expressionTemp));
						sTemp.delete();
						ifTemp.setThenStatement(ys);
					} else if (sTemp instanceof Block) {
						Block block = (Block) sTemp;
						int index = block.statements().size() - 1;
						Statement sTempTemp = (Statement) block.statements()
								.get(index);
						if (sTempTemp instanceof IfStatement) {
							refactorEndIfA((IfStatement) sTempTemp);
						} else if (sTempTemp instanceof ExpressionStatement
								|| sTempTemp instanceof YieldStatement) {
							Expression expre = null;
							if (sTempTemp instanceof ExpressionStatement) {
								expre = ((ExpressionStatement) sTempTemp)
										.getExpression();
							} else {
								expre = ((YieldStatement) sTempTemp)
										.getExpression();
							}
							Expression eTemp = (Expression) ((Assignment) expre)
									.getRightHandSide();
							Expression expressionTemp = eTemp;
							YieldStatement ys = ast.newYieldStatement();
							ys.setExpression((Expression) ASTNode
									.copySubtree(ast, expressionTemp));
							sTempTemp.delete();
							block.statements().add(ys);
						}
					} else if (sTemp instanceof IfStatement) {
						refactorEndIfA((IfStatement) sTemp);
					}
				}
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void refactorEndIfR(IfStatement ifTemp) {
		AST ast = SwitchRefactoring.astFlag;
		while (true) {
			Statement s = ifTemp.getThenStatement();
			if (!(s instanceof ThrowStatement)) {
				if (s instanceof ReturnStatement) {
					Expression eTemp = ((ReturnStatement) s).getExpression();
					eTemp.delete();
					Expression expressionTemp = eTemp;
					YieldStatement ys = ast.newYieldStatement();
					ys.setExpression(expressionTemp);
					// s.delete();
					ifTemp.setThenStatement(ys);
				} else if (s instanceof Block) {
					Block block = (Block) s;
					int index = block.statements().size() - 1;
					Statement sTemp = (Statement) block.statements().get(index);
					if (sTemp instanceof IfStatement) {
						refactorEndIfR((IfStatement) sTemp);
					} else if (sTemp instanceof ReturnStatement) {
						Expression eTemp = ((ReturnStatement) sTemp)
								.getExpression();
						eTemp.delete();
						Expression expressionTemp = eTemp;
						YieldStatement ys = ast.newYieldStatement();
						ys.setExpression(expressionTemp);
						sTemp.delete();
						block.statements().add(ys);
					}
				} else if (s instanceof IfStatement) {
					refactorEndIfR((IfStatement) s);
				}
			}
			Statement sTemp = ifTemp.getElseStatement();
			if (sTemp instanceof IfStatement) {
				ifTemp = (IfStatement) sTemp;
			} else {
				if (!(sTemp instanceof ThrowStatement)) {
					if (sTemp instanceof ReturnStatement) {
						Expression eTemp = ((ReturnStatement) sTemp)
								.getExpression();
						eTemp.delete();
						Expression expressionTemp = eTemp;
						YieldStatement ys = ast.newYieldStatement();
						ys.setExpression(expressionTemp);
						sTemp.delete();
						ifTemp.setThenStatement(ys);
					} else if (sTemp instanceof Block) {
						Block block = (Block) sTemp;
						int index = block.statements().size() - 1;
						Statement sTempTemp = (Statement) block.statements()
								.get(index);
						if (sTempTemp instanceof IfStatement) {
							refactorEndIfR((IfStatement) sTempTemp);
						} else if (sTempTemp instanceof ReturnStatement) {
							Expression eTemp = ((ReturnStatement) sTempTemp)
									.getExpression();
							eTemp.delete();
							Expression expressionTemp = eTemp;
							YieldStatement ys = ast.newYieldStatement();
							ys.setExpression(expressionTemp);
							sTempTemp.delete();
							block.statements().add(ys);
						}
					} else if (sTemp instanceof IfStatement) {
						refactorEndIfR((IfStatement) sTemp);
					}
				}
				break;
			}
		}
	}

	public static boolean checkMidIf(Expression e,
			LinkedHashMap<Integer, Block> blockMap) {
		int label = 1;
		boolean flag = false;
		while (blockMap.containsKey(label)) {
			Block blockTemp = blockMap.get(label);
			int index = blockTemp.statements().size() - 1;
			if (blockTemp.statements().size() > 1) {
				for (int i = 0; i < index; i++) {
					Statement sTemp = (Statement) blockTemp.statements().get(i);
					if (sTemp instanceof IfStatement) {
						IfStatement ifTemp = (IfStatement) sTemp;
						if (e instanceof MethodInvocation) {
							flag = checkRefactorMidIfM(ifTemp,
									(MethodInvocation) e);
							if (!flag) {
								return false;
							} else {
								refactorMidIfM(ifTemp);
							}
						} else if (e instanceof Assignment) {
							flag = checkRefactorMidIfA(ifTemp, (Assignment) e);
							if (!flag) {
								return false;
							} else {
								refactorMidIfA(ifTemp);
							}
						} else {
							flag = checkRefactorMidIfR(ifTemp);
							if (!flag) {
								return false;
							} else {
								refactorMidIfR(ifTemp);
							}
						}
					}
				}
			}
			label++;
		}
		return true;
	}

	private static boolean checkMidIfBlock(Statement s, int index,
			Expression e) {
		boolean flag = false;
		Block blockTemp = (Block) s;
		for (int i = 0; i <= index; i++) {
			Statement sTemp = (Statement) blockTemp.statements().get(i);
			if (sTemp instanceof IfStatement) {
				IfStatement ifTemp = (IfStatement) sTemp;
				if (e instanceof MethodInvocation) {
					flag = checkRefactorMidIfM(ifTemp, (MethodInvocation) e);
					if (!flag) {
						return false;
					} else {
						refactorMidIfM(ifTemp);
					}
				} else if (e instanceof Assignment) {
					flag = checkRefactorMidIfA(ifTemp, (Assignment) e);
					if (!flag) {
						return false;
					} else {
						refactorMidIfA(ifTemp);
					}
				} else {
					flag = checkRefactorMidIfR(ifTemp);
					if (!flag) {
						return false;
					} else {
						refactorMidIfR(ifTemp);
					}
				}
			}
		}
		return true;
	}

	private static boolean checkRefactorMidIfR(IfStatement ifTemp) {
		while (true) {
			Statement s = ifTemp.getThenStatement();
			if (!(s instanceof ThrowStatement)) {
				if (s instanceof BreakStatement) {
					return false;
				} else if (s instanceof ReturnStatement) {

				} else if (s instanceof Block) {
					Block block = (Block) s;
					int index = block.statements().size() - 1;
					if (index >= 0) {
						Statement sTemp = (Statement) block.statements()
								.get(index);
						if (sTemp instanceof IfStatement) {
							boolean labelTemp = checkRefactorMidIfR(
									(IfStatement) sTemp);
							if (!labelTemp) {
								return false;
							}
						} else if (sTemp instanceof ReturnStatement) {

						} else if (sTemp instanceof BreakStatement) {
							return false;
						}
						// 检查block中其他if
						if (index > 0) {
							if (!checkMidIfBlock(s, index - 1, null))
								return false;
						}
					}
				} else if (s instanceof IfStatement) {
					boolean labelTemp = checkRefactorMidIfR((IfStatement) s);
					if (!labelTemp) {
						return false;
					}
				}
			}
			Statement sTemp = ifTemp.getElseStatement();
			if (sTemp == null) {
				break;
			} else if (sTemp instanceof IfStatement) {
				ifTemp = (IfStatement) sTemp;
			} else {
				if (!(sTemp instanceof ThrowStatement)) {
					if (sTemp instanceof BreakStatement) {
						return false;
					} else if (sTemp instanceof ReturnStatement) {

					} else if (sTemp instanceof Block) {
						Block block = (Block) sTemp;
						int index = block.statements().size() - 1;
						if (index >= 0) {
							Statement sTempTemp = (Statement) block.statements()
									.get(index);
							if (sTempTemp instanceof IfStatement) {
								boolean labelTemp = checkRefactorMidIfR(
										(IfStatement) sTempTemp);
								if (!labelTemp) {
									return false;
								}
							} else if (sTempTemp instanceof ReturnStatement) {

							} else if (sTempTemp instanceof BreakStatement) {
								return false;
							}
							// 检查block中其他if
							if (index > 0) {
								if (!checkMidIfBlock(sTemp, index - 1, null))
									return false;
							}
						}
					}
				}
				break;
			}
		}
		return true;
	}

	private static boolean checkRefactorMidIfA(IfStatement ifTemp,
			Assignment e) {
		while (true) {
			Statement s = ifTemp.getThenStatement();
			if (!(s instanceof ThrowStatement)) {
				if (s instanceof BreakStatement) {
					return false;
				} else if (s instanceof ReturnStatement) {
					return false;
				} else if (s instanceof Block) {
					Block block = (Block) s;
					if (block.statements().size() > 1) {
						int index = block.statements().size() - 1;
						if (block.statements()
								.get(index) instanceof BreakStatement) {
							Statement sTemp = (Statement) block.statements()
									.get(index - 1);
							if (sTemp instanceof ExpressionStatement
									|| sTemp instanceof YieldStatement) {
								Expression expre = null;
								if (sTemp instanceof ExpressionStatement) {
									expre = ((ExpressionStatement) sTemp)
											.getExpression();
								} else {
									expre = ((YieldStatement) sTemp)
											.getExpression();
								}
								if (expre instanceof Assignment) {
									Assignment aTemp = (Assignment) expre;
									if (aTemp.getLeftHandSide().toString()
											.equals(e.getLeftHandSide()
													.toString())
											&& aTemp.getOperator() == Assignment.Operator.ASSIGN) {

									} else {
										return false;
									}
								} else {
									return false;
								}
							} else {
								return false;
							}
							// 检查block中其他if
							if (index > 0) {
								if (!checkMidIfBlock(s, index - 2, e))
									return false;
							}
						} else if (block.statements()
								.get(index) instanceof IfStatement) {
							boolean labelTemp = checkRefactorMidIfA(
									(IfStatement) block.statements().get(index),
									e);
							if (!labelTemp) {
								return false;
							}
						} else if (block.statements()
								.get(index) instanceof ReturnStatement) {
							return false;
						}
						// 检查block中其他if
						if (index > 0) {
							if (!checkMidIfBlock(s, index - 1, e))
								return false;
						}
					} else if (block.statements().size() == 1) {
						if (block.statements()
								.get(0) instanceof BreakStatement) {
							return false;
						} else if (block.statements()
								.get(0) instanceof ReturnStatement) {
							return false;
						} else if (block.statements()
								.get(0) instanceof IfStatement) {
							boolean labelTemp = checkRefactorMidIfA(
									(IfStatement) block.statements().get(0), e);
							if (!labelTemp) {
								return false;
							}
						}
					}
				} else if (s instanceof IfStatement) {
					boolean labelTemp = checkRefactorMidIfA((IfStatement) s, e);
					if (!labelTemp) {
						return false;
					}
				}
			}
			Statement sTemp = ifTemp.getElseStatement();
			if (sTemp == null) {
				break;
			} else if (sTemp instanceof IfStatement) {
				ifTemp = (IfStatement) sTemp;
			} else {
				if (!(sTemp instanceof ThrowStatement)) {
					if (sTemp instanceof BreakStatement) {
						return false;
					} else if (sTemp instanceof ReturnStatement) {
						return false;
					} else if (sTemp instanceof Block) {
						Block block = (Block) sTemp;
						if (block.statements().size() > 1) {
							int index = block.statements().size() - 1;
							if (block.statements()
									.get(index) instanceof BreakStatement) {
								Statement sTempTemp = (Statement) block
										.statements().get(index - 1);
								if (sTempTemp instanceof ExpressionStatement
										|| sTempTemp instanceof YieldStatement) {
									Expression expre = null;
									if (sTempTemp instanceof ExpressionStatement) {
										expre = ((ExpressionStatement) sTempTemp)
												.getExpression();
									} else {
										expre = ((YieldStatement) sTempTemp)
												.getExpression();
									}
									if (expre instanceof Assignment) {
										Assignment aTemp = (Assignment) expre;
										if (aTemp.getLeftHandSide().toString()
												.equals(e.getLeftHandSide()
														.toString())
												&& aTemp.getOperator() == Assignment.Operator.ASSIGN) {

										} else {
											return false;
										}
									} else {
										return false;
									}
								} else {
									return false;
								}
								// 检查block中其他if
								if (index > 0) {
									if (!checkMidIfBlock(sTemp, index - 2, e))
										return false;
								}
							} else if (block.statements()
									.get(index) instanceof IfStatement) {
								boolean labelTemp = checkRefactorMidIfA(
										(IfStatement) block.statements()
												.get(index),
										e);
								if (!labelTemp) {
									return false;
								}
							} else if (block.statements()
									.get(index) instanceof ReturnStatement) {
								return false;
							}
							// 检查block中其他if
							if (index > 0) {
								if (!checkMidIfBlock(sTemp, index - 1, e))
									return false;
							}
						} else if (block.statements().size() == 1) {
							if (block.statements()
									.get(0) instanceof BreakStatement) {
								return false;
							} else if (block.statements()
									.get(0) instanceof ReturnStatement) {
								return false;
							} else if (block.statements()
									.get(0) instanceof IfStatement) {
								boolean labelTemp = checkRefactorMidIfA(
										(IfStatement) block.statements().get(0),
										e);
								if (!labelTemp) {
									return false;
								}
							}
						}
					}
				}
				break;
			}
		}
		return true;
	}

	private static boolean checkRefactorMidIfM(IfStatement ifTemp,
			MethodInvocation e) {
		while (true) {
			Statement s = ifTemp.getThenStatement();
			if (!(s instanceof ThrowStatement)) {
				if (s instanceof BreakStatement) {
					return false;
				} else if (s instanceof ReturnStatement) {
					return false;
				} else if (s instanceof Block) {
					Block block = (Block) s;
					if (block.statements().size() > 1) {
						int index = block.statements().size() - 1;
						if (block.statements()
								.get(index) instanceof BreakStatement) {
							Statement sTemp = (Statement) block.statements()
									.get(index - 1);
							if (sTemp instanceof ExpressionStatement
									|| sTemp instanceof YieldStatement) {
								Expression expre = null;
								if (sTemp instanceof ExpressionStatement) {
									expre = ((ExpressionStatement) sTemp)
											.getExpression();
								} else {
									expre = ((YieldStatement) sTemp)
											.getExpression();
								}
								if (expre instanceof MethodInvocation) {
									MethodInvocation mTemp = (MethodInvocation) expre;
									if (mTemp.arguments().size() == 1 && mTemp
											.getName().toString()
											.equals(e.getName().toString())) {
										if ((mTemp.getExpression() == null
												&& e.getExpression() == null)
												|| (mTemp
														.getExpression() != null
														&& e.getExpression() != null)) {
											if (mTemp.getExpression() != null) {
												if (!(mTemp.getExpression()
														.toString()
														.equals(e
																.getExpression()
																.toString()))) {
													return false;
												}
											}
										} else {
											return false;
										}
									} else {
										return false;
									}
								} else {
									return false;
								}
							} else {
								return false;
							}
							// 检查block中其他if
							if (index > 0) {
								if (!checkMidIfBlock(s, index - 2, e)) {
									return false;
								}
							}
						} else if (block.statements()
								.get(index) instanceof IfStatement) {
							boolean labelTemp = checkRefactorMidIfM(
									(IfStatement) block.statements().get(index),
									e);
							if (!labelTemp) {
								return false;
							}
						} else if (block.statements()
								.get(index) instanceof ReturnStatement) {
							return false;
						}
						// 检查block中其他if
						if (index > 0) {
							if (!checkMidIfBlock(s, index - 1, e))
								return false;
						}
					} else if (block.statements().size() == 1) {
						if (block.statements()
								.get(0) instanceof BreakStatement) {
							return false;
						} else if (block.statements()
								.get(0) instanceof ReturnStatement) {
							return false;
						} else if (block.statements()
								.get(0) instanceof IfStatement) {
							boolean labelTemp = checkRefactorMidIfM(
									(IfStatement) block.statements().get(0), e);
							if (!labelTemp) {
								return false;
							}
						}
					}
				} else if (s instanceof IfStatement) {
					boolean labelTemp = checkRefactorMidIfM((IfStatement) s, e);
					if (!labelTemp) {
						return false;
					}
				}
			}
			Statement sTemp = ifTemp.getElseStatement();
			if (sTemp == null) {
				break;
			} else if (sTemp instanceof IfStatement) {
				ifTemp = (IfStatement) sTemp;
			} else {
				if (!(sTemp instanceof ThrowStatement)) {
					if (sTemp instanceof BreakStatement) {
						return false;
					} else if (sTemp instanceof ReturnStatement) {
						return false;
					} else if (sTemp instanceof Block) {
						Block block = (Block) sTemp;
						if (block.statements().size() > 1) {
							int index = block.statements().size() - 1;
							if (block.statements()
									.get(index) instanceof BreakStatement) {
								Statement sTempTemp = (Statement) block
										.statements().get(index - 1);
								if (sTempTemp instanceof ExpressionStatement
										|| sTempTemp instanceof YieldStatement) {
									Expression expre = null;
									if (sTempTemp instanceof ExpressionStatement) {
										expre = ((ExpressionStatement) sTempTemp)
												.getExpression();
									} else {
										expre = ((YieldStatement) sTempTemp)
												.getExpression();
									}
									if (expre instanceof MethodInvocation) {
										MethodInvocation mTemp = (MethodInvocation) expre;
										if (mTemp.arguments().size() == 1
												&& mTemp.getName().toString()
														.equals(e.getName()
																.toString())) {
											if ((mTemp.getExpression() == null
													&& e.getExpression() == null)
													|| (mTemp
															.getExpression() != null
															&& e.getExpression() != null)) {
												if (mTemp
														.getExpression() != null) {
													if (!(mTemp.getExpression()
															.toString()
															.equals(e
																	.getExpression()
																	.toString()))) {
														return false;
													}
												}
											} else {
												return false;
											}
										} else {
											return false;
										}
									} else {
										return false;
									}
								} else {
									return false;
								}
								// 检查block中其他if
								if (index > 0) {
									if (!checkMidIfBlock(sTemp, index - 2, e))
										return false;
								}
							} else if (block.statements()
									.get(index) instanceof IfStatement) {
								boolean labelTemp = checkRefactorMidIfM(
										(IfStatement) block.statements()
												.get(index),
										e);
								if (!labelTemp) {
									return false;
								}
							} else if (block.statements()
									.get(index) instanceof ReturnStatement) {
								return false;
							}
							// 检查block中其他if
							if (index > 0) {
								if (!checkMidIfBlock(sTemp, index - 1, e))
									return false;
							}
						} else if (block.statements().size() == 1) {
							if (block.statements()
									.get(0) instanceof BreakStatement) {
								return false;
							} else if (block.statements()
									.get(0) instanceof ReturnStatement) {
								return false;
							} else if (block.statements()
									.get(0) instanceof IfStatement) {
								boolean labelTemp = checkRefactorMidIfM(
										(IfStatement) block.statements().get(0),
										e);
								if (!labelTemp) {
									return false;
								}
							}
						}
					}
				}
				break;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private static boolean refactorMidIfR(IfStatement ifTemp) {
		AST ast = SwitchRefactoring.astFlag;
		while (true) {
			Statement s = ifTemp.getThenStatement();
			if (!(s instanceof ThrowStatement)) {
				if (s instanceof ReturnStatement) {
					Expression eTemp = ((ReturnStatement) s).getExpression();
					YieldStatement ys = ast.newYieldStatement();
					ys.setExpression(
							(Expression) ASTNode.copySubtree(ast, eTemp));
					s.delete();
					ifTemp.setThenStatement(ys);
				} else if (s instanceof Block) {
					Block block = (Block) s;
					int index = block.statements().size() - 1;
					Statement sTemp = (Statement) block.statements().get(index);
					if (sTemp instanceof IfStatement) {
						refactorMidIfR((IfStatement) sTemp);
					} else if (sTemp instanceof ReturnStatement) {
						Expression eTemp = ((ReturnStatement) sTemp)
								.getExpression();
						YieldStatement ys = ast.newYieldStatement();
						ys.setExpression(
								(Expression) ASTNode.copySubtree(ast, eTemp));
						sTemp.delete();
						block.statements().add(ys);
					}
				} else if (s instanceof IfStatement) {
					refactorMidIfR((IfStatement) s);
				}
			}
			Statement sTemp = ifTemp.getElseStatement();
			if (sTemp == null) {
				break;
			} else if (sTemp instanceof IfStatement) {
				ifTemp = (IfStatement) sTemp;
			} else {
				if (!(sTemp instanceof ThrowStatement)) {
					if (sTemp instanceof ReturnStatement) {
						Expression eTemp = ((ReturnStatement) sTemp)
								.getExpression();
						YieldStatement ys = ast.newYieldStatement();
						ys.setExpression(
								(Expression) ASTNode.copySubtree(ast, eTemp));
						sTemp.delete();
						ifTemp.setThenStatement(ys);
					} else if (sTemp instanceof Block) {
						Block block = (Block) sTemp;
						int index = block.statements().size() - 1;
						Statement sTempTemp = (Statement) block.statements()
								.get(index);
						if (sTempTemp instanceof IfStatement) {
							refactorMidIfR((IfStatement) sTempTemp);
						} else if (sTempTemp instanceof ReturnStatement) {
							Expression eTemp = ((ReturnStatement) sTempTemp)
									.getExpression();
							YieldStatement ys = ast.newYieldStatement();
							ys.setExpression((Expression) ASTNode
									.copySubtree(ast, eTemp));
							sTempTemp.delete();
							block.statements().add(ys);
						}
					}
				}
				break;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private static boolean refactorMidIfA(IfStatement ifTemp) {
		AST ast = SwitchRefactoring.astFlag;
		while (true) {
			Statement s = ifTemp.getThenStatement();
			if (!(s instanceof ThrowStatement)) {
				if (s instanceof Block) {
					Block block = (Block) s;
					if (block.statements().size() > 1) {
						int index = block.statements().size() - 1;
						if (block.statements()
								.get(index) instanceof BreakStatement) {
							Statement sTemp = (Statement) block.statements()
									.get(index - 1);
							if (sTemp instanceof ExpressionStatement
									|| sTemp instanceof YieldStatement) {
								Expression expre = null;
								if (sTemp instanceof ExpressionStatement) {
									expre = ((ExpressionStatement) sTemp)
											.getExpression();
								} else {
									expre = ((YieldStatement) sTemp)
											.getExpression();
								}
								if (expre instanceof Assignment) {
									Assignment aTemp = (Assignment) expre;
									YieldStatement ys = ast.newYieldStatement();
									ys.setExpression((Expression) ASTNode
											.copySubtree(ast,
													aTemp.getRightHandSide()));
									block.statements().remove(index);
									block.statements().remove(index - 1);
									block.statements().add(ys);
								}
							}
						} else if (block.statements()
								.get(index) instanceof IfStatement) {
							refactorMidIfA((IfStatement) block.statements()
									.get(index));
						}
					} else if (block.statements().size() == 1) {
						if (block.statements().get(0) instanceof IfStatement) {
							refactorMidIfA(
									(IfStatement) block.statements().get(0));
						}
					}
				} else if (s instanceof IfStatement) {
					refactorMidIfA((IfStatement) s);
				}
			}
			Statement sTemp = ifTemp.getElseStatement();
			if (sTemp == null) {
				break;
			} else if (sTemp instanceof IfStatement) {
				ifTemp = (IfStatement) sTemp;
			} else {
				if (!(sTemp instanceof ThrowStatement)) {
					if (s instanceof Block) {
						Block block = (Block) s;
						if (block.statements().size() > 1) {
							int index = block.statements().size() - 1;
							if (block.statements()
									.get(index) instanceof BreakStatement) {
								Statement sTempTemp = (Statement) block
										.statements().get(index - 1);
								if (sTempTemp instanceof ExpressionStatement
										|| sTempTemp instanceof YieldStatement) {
									Expression expre = null;
									if (sTempTemp instanceof ExpressionStatement) {
										expre = ((ExpressionStatement) sTempTemp)
												.getExpression();
									} else {
										expre = ((YieldStatement) sTempTemp)
												.getExpression();
									}
									if (expre instanceof Assignment) {
										Assignment aTemp = (Assignment) expre;
										YieldStatement ys = ast
												.newYieldStatement();
										ys.setExpression((Expression) ASTNode
												.copySubtree(ast, aTemp
														.getRightHandSide()));
										block.statements().remove(index);
										block.statements().remove(index - 1);
										block.statements().add(ys);
									}
								}
							} else if (block.statements()
									.get(index) instanceof IfStatement) {
								refactorMidIfA((IfStatement) block.statements()
										.get(index));
							}
						} else if (block.statements().size() == 1) {
							if (block.statements()
									.get(0) instanceof IfStatement) {
								refactorMidIfA((IfStatement) block.statements()
										.get(0));
							}
						}
					}
				}
				break;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private static boolean refactorMidIfM(IfStatement ifTemp) {
		AST ast = SwitchRefactoring.astFlag;
		while (true) {
			Statement s = ifTemp.getThenStatement();
			if (!(s instanceof ThrowStatement)) {
				if (s instanceof Block) {
					Block block = (Block) s;
					if (block.statements().size() > 1) {
						int index = block.statements().size() - 1;
						if (block.statements()
								.get(index) instanceof BreakStatement) {
							Statement sTemp = (Statement) block.statements()
									.get(index - 1);
							if (sTemp instanceof ExpressionStatement
									|| sTemp instanceof YieldStatement) {
								Expression expre = null;
								if (sTemp instanceof ExpressionStatement) {
									expre = ((ExpressionStatement) sTemp)
											.getExpression();
								} else {
									expre = ((YieldStatement) sTemp)
											.getExpression();
								}
								if (expre instanceof MethodInvocation) {
									MethodInvocation mTemp = (MethodInvocation) expre;
									YieldStatement ys = ast.newYieldStatement();
									ys.setExpression((Expression) ASTNode
											.copySubtree(ast, (Expression) mTemp
													.arguments().get(0)));
									block.statements().remove(index);
									block.statements().remove(index - 1);
									block.statements().add(ys);
								}
							}
						} else if (block.statements()
								.get(index) instanceof IfStatement) {
							refactorMidIfM((IfStatement) block.statements()
									.get(index));
						}
					} else if (block.statements().size() == 1) {
						if (block.statements().get(0) instanceof IfStatement) {
							refactorMidIfM(
									(IfStatement) block.statements().get(0));
						}
					}
				} else if (s instanceof IfStatement) {
					refactorMidIfM((IfStatement) s);
				}
			}
			Statement sTemp = ifTemp.getElseStatement();
			if (sTemp == null) {
				break;
			} else if (sTemp instanceof IfStatement) {
				ifTemp = (IfStatement) sTemp;
			} else {
				if (!(sTemp instanceof ThrowStatement)) {
					if (s instanceof Block) {
						Block block = (Block) s;
						if (block.statements().size() > 1) {
							int index = block.statements().size() - 1;
							if (block.statements()
									.get(index) instanceof BreakStatement) {
								Statement sTempTemp = (Statement) block
										.statements().get(index - 1);
								if (sTempTemp instanceof ExpressionStatement
										|| sTempTemp instanceof YieldStatement) {
									Expression expre = null;
									if (sTempTemp instanceof ExpressionStatement) {
										expre = ((ExpressionStatement) sTempTemp)
												.getExpression();
									} else {
										expre = ((YieldStatement) sTempTemp)
												.getExpression();
									}
									if (expre instanceof MethodInvocation) {
										MethodInvocation mTemp = (MethodInvocation) expre;
										YieldStatement ys = ast
												.newYieldStatement();
										ys.setExpression((Expression) ASTNode
												.copySubtree(ast,
														(Expression) mTemp
																.arguments()
																.get(0)));
										block.statements().remove(index);
										block.statements().remove(index - 1);
										block.statements().add(ys);
									}
								}
							} else if (block.statements()
									.get(index) instanceof IfStatement) {
								refactorMidIfM((IfStatement) block.statements()
										.get(index));
							}
						} else if (block.statements().size() == 1) {
							if (block.statements()
									.get(0) instanceof IfStatement) {
								refactorMidIfM((IfStatement) block.statements()
										.get(0));
							}
						}
					}
				}
				break;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static boolean checkEndTry(TryStatement tTemp) {
		boolean labelTry = true;
		boolean labelCatch = true;
		Block bTemp = null;
		bTemp = tTemp.getBody();
		if (bTemp.statements().size() > 0
				&& bTemp.statements().get(
						bTemp.statements().size() - 1) instanceof BreakStatement
				|| bTemp.statements().size() > 0 && bTemp.statements()
						.get(bTemp.statements().size()
								- 1) instanceof ReturnStatement
				|| bTemp.statements().size() > 0 && bTemp.statements().get(
						bTemp.statements().size() - 1) instanceof ThrowStatement
				|| bTemp.statements().size() > 0
						&& bTemp.statements().get(bTemp.statements().size()
								- 1) instanceof ContinueStatement) {

		} else if (bTemp.statements().size() > 0 && bTemp.statements()
				.get(bTemp.statements().size() - 1) instanceof IfStatement) {
			IfStatement ifTemp = (IfStatement) bTemp.statements()
					.get(bTemp.statements().size() - 1);
			if (!checkEndIf(ifTemp, true)) {
				return false;
			}
		} else {
			labelTry = false;
		}
		List<CatchClause> list = tTemp.catchClauses();
//		if (list.size() == 0)
//			return false;
		for (CatchClause cc : list) {
			bTemp = cc.getBody();
			if (bTemp.statements().size() > 0
					&& bTemp.statements()
							.get(bTemp.statements().size()
									- 1) instanceof BreakStatement
					|| bTemp.statements().size() > 0 && bTemp.statements()
							.get(bTemp.statements().size()
									- 1) instanceof ReturnStatement
					|| bTemp.statements().size() > 0 && bTemp.statements()
							.get(bTemp.statements().size()
									- 1) instanceof ThrowStatement
					|| bTemp.statements().size() > 0
							&& bTemp.statements().get(bTemp.statements().size()
									- 1) instanceof ContinueStatement) {

			} else if (bTemp.statements().size() > 0 && bTemp.statements().get(
					bTemp.statements().size() - 1) instanceof IfStatement) {
				IfStatement ifTemp = (IfStatement) bTemp.statements()
						.get(bTemp.statements().size() - 1);
				if (!checkEndIf(ifTemp, true)) {
					return false;
				}
			} else {
				labelCatch = false;
			}
		}

		if (!labelTry || !labelCatch) {
			bTemp = tTemp.getFinally();
			if (bTemp != null) {
				if (bTemp.statements().size() > 0
						&& bTemp.statements()
								.get(bTemp.statements().size()
										- 1) instanceof BreakStatement
						|| bTemp.statements().size() > 0 && bTemp.statements()
								.get(bTemp.statements().size()
										- 1) instanceof ReturnStatement
						|| bTemp.statements().size() > 0 && bTemp.statements()
								.get(bTemp.statements().size()
										- 1) instanceof ThrowStatement
						|| bTemp.statements().size() > 0 && bTemp.statements()
								.get(bTemp.statements().size()
										- 1) instanceof ContinueStatement) {

				} else if (bTemp.statements().size() > 0
						&& bTemp.statements().get(bTemp.statements().size()
								- 1) instanceof IfStatement) {
					IfStatement ifTemp = (IfStatement) bTemp.statements()
							.get(bTemp.statements().size() - 1);
					if (!checkEndIf(ifTemp, true)) {
						return false;
					}
				} else {
					return false;
				}
			}else {
				return false;
			}
		}

		SwitchRefactoring.deleteTryBreakLabel = true;

		return true;
	}
}